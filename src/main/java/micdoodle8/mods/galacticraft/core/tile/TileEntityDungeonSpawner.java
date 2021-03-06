package micdoodle8.mods.galacticraft.core.tile;

import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.core.entities.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class TileEntityDungeonSpawner<E extends Entity> extends TileEntityAdvanced
{
    public Class<E> bossClass;
    public IBoss boss;
    public boolean spawned;
    public boolean isBossDefeated;
    public boolean playerInRange;
    public boolean lastPlayerInRange;
    public boolean playerCheated;
    private Vector3 roomCoords;
    private Vector3 roomSize;
    public long lastKillTime;

    public TileEntityDungeonSpawner()
    {
    }

    public TileEntityDungeonSpawner(Class<E> bossClass)
    {
        this.bossClass = bossClass;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void update()
    {
        super.update();

        if (this.roomCoords == null)
        {
            return;
        }

        if (!this.worldObj.isRemote)
        {
            if (this.lastKillTime > 0 && MinecraftServer.getCurrentTimeMillis() - lastKillTime > 900000) // 15 minutes
            {
                this.lastKillTime = 0;
                this.isBossDefeated = false;
            }

            final Vector3 thisVec = new Vector3(this);
            final List<E> l = this.worldObj.getEntitiesWithinAABB(bossClass, AxisAlignedBB.fromBounds(thisVec.x - 15, thisVec.y - 15, thisVec.z - 15, thisVec.x + 15, thisVec.y + 15, thisVec.z + 15));

            for (final Entity e : l)
            {
                if (!e.isDead)
                {
                    this.boss = (IBoss) e;
                    this.boss.setRoom(this.roomCoords, this.roomSize);
                    this.spawned = true;
                    this.isBossDefeated = false;
                }
            }

            List<EntityMob> entitiesWithin = this.worldObj.getEntitiesWithinAABB(EntityMob.class, AxisAlignedBB.fromBounds(this.roomCoords.intX() - 3, this.roomCoords.intY() - 3, this.roomCoords.intZ() - 3, this.roomCoords.intX() + this.roomSize.intX() + 3, this.roomCoords.intY() + this.roomSize.intY() + 3, this.roomCoords.intZ() + this.roomSize.intZ() + 3));

            for (Entity mob : entitiesWithin)
            {
                if (this.getDisabledCreatures().contains(mob.getClass()))
                {
                    mob.setDead();
                }
            }

            List<EntityPlayer> playersWithin = this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.fromBounds(this.roomCoords.intX(), this.roomCoords.intY(), this.roomCoords.intZ(), this.roomCoords.intX() + this.roomSize.intX(), this.roomCoords.intY() + this.roomSize.intY(), this.roomCoords.intZ() + this.roomSize.intZ()));

            if (this.boss == null && !this.isBossDefeated && !playersWithin.isEmpty())
            {
                try
                {
                    Constructor<?> c = this.bossClass.getConstructor(new Class[] { World.class });
                    this.boss = (IBoss) c.newInstance(new Object[] { this.worldObj });
                    ((Entity) this.boss).setPosition(this.getPos().getX() + 0.5, this.getPos().getY() + 1.0, this.getPos().getZ() + 0.5);
                    this.boss.setRoom(this.roomCoords, this.roomSize);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            if (this.playerCheated)
            {
                if (!playersWithin.isEmpty())
                {
                    this.isBossDefeated = false;
                    this.spawned = false;
                    this.lastPlayerInRange = false;
                    this.playerCheated = false;
                }
            }
            else if (playersWithin.size() == 0)
            {
                this.spawned = false;
                this.lastPlayerInRange = false;
                this.playerCheated = false;
            }

            this.playerInRange = !playersWithin.isEmpty();

            if (this.playerInRange && !this.lastPlayerInRange)
            {
                if (this.boss != null && !this.spawned)
                {
                    if (this.boss instanceof EntityLiving)
                    {
                        EntityLiving bossLiving = (EntityLiving) this.boss;
                        bossLiving.onInitialSpawn(this.worldObj.getDifficultyForLocation(new BlockPos(bossLiving)), null);
                        this.worldObj.spawnEntityInWorld(bossLiving);
                        this.playSpawnSound(bossLiving);
                        this.spawned = true;
                        this.boss.onBossSpawned(this);
                        this.boss.setRoom(this.roomCoords, this.roomSize);
                    }
                }
            }

            if (this.boss != null && ((EntityLiving) this.boss).isDead)
            {
                this.isBossDefeated = false;
                this.spawned = false;
                this.lastPlayerInRange = false;
                this.playerCheated = false;
                this.boss = null;
            }

            this.lastPlayerInRange = this.playerInRange;
        }
    }

    public void playSpawnSound(Entity entity)
    {

    }

    public List<Class<? extends EntityLiving>> getDisabledCreatures()
    {
        List<Class<? extends EntityLiving>> list = new ArrayList<Class<? extends EntityLiving>>();
        list.add(EntityEvolvedSkeleton.class);
        list.add(EntityEvolvedCreeper.class);
        list.add(EntityEvolvedZombie.class);
        list.add(EntityEvolvedSpider.class);
        return list;
    }

    public void setRoom(Vector3 coords, Vector3 size)
    {
        this.roomCoords = coords;
        this.roomSize = size;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        this.spawned = nbt.getBoolean("spawned");
        this.playerInRange = this.lastPlayerInRange = nbt.getBoolean("playerInRange");
        this.isBossDefeated = nbt.getBoolean("defeated");
        this.playerCheated = nbt.getBoolean("playerCheated");

        try
        {
            this.bossClass = (Class<E>) Class.forName(nbt.getString("bossClass"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        this.roomCoords = new Vector3();
        this.roomCoords.x = nbt.getDouble("roomCoordsX");
        this.roomCoords.y = nbt.getDouble("roomCoordsY");
        this.roomCoords.z = nbt.getDouble("roomCoordsZ");
        this.roomSize = new Vector3();
        this.roomSize.x = nbt.getDouble("roomSizeX");
        this.roomSize.y = nbt.getDouble("roomSizeY");
        this.roomSize.z = nbt.getDouble("roomSizeZ");

        if (nbt.hasKey("lastKillTime"))
        {
            this.lastKillTime = nbt.getLong("lastKillTime");
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setBoolean("spawned", this.spawned);
        nbt.setBoolean("playerInRange", this.playerInRange);
        nbt.setBoolean("defeated", this.isBossDefeated);
        nbt.setBoolean("playerCheated", this.playerCheated);
        nbt.setString("bossClass", this.bossClass.getCanonicalName());

        if (this.roomCoords != null)
        {
            nbt.setDouble("roomCoordsX", this.roomCoords.x);
            nbt.setDouble("roomCoordsY", this.roomCoords.y);
            nbt.setDouble("roomCoordsZ", this.roomCoords.z);
            nbt.setDouble("roomSizeX", this.roomSize.x);
            nbt.setDouble("roomSizeY", this.roomSize.y);
            nbt.setDouble("roomSizeZ", this.roomSize.z);
        }

        nbt.setLong("lastKillTime", this.lastKillTime);
    }

    @Override
    public double getPacketRange()
    {
        return 0;
    }

    @Override
    public int getPacketCooldown()
    {
        return 0;
    }

    @Override
    public boolean isNetworkedTile()
    {
        return false;
    }
}
