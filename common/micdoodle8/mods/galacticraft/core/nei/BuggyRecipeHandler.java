package micdoodle8.mods.galacticraft.core.nei;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.forge.GuiContainerManager;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class BuggyRecipeHandler extends TemplateRecipeHandler
{
    public String getRecipeId()
    {
        return "galacticraft.buggy";
    }

    @Override
    public int recipiesPerPage()
    {
        return 1;
    }

    public Set<Entry<ArrayList<PositionedStack>, PositionedStack>> getRecipes()
    {
        return NEIGalacticraftConfig.getBuggyBenchRecipes();
    }

    @Override
    public void drawBackground(GuiContainerManager guimanager, int i)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        guimanager.bindTexture(this.getGuiTexture());
        guimanager.drawTexturedModalRect(0, 0, 3, 4, 168, 130);
    }

    @Override
    public void drawExtras(GuiContainerManager guimanager, int i)
    {
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
    }

    @Override
    public void loadTransferRects()
    {
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results)
    {
        if (outputId.equals(this.getRecipeId()))
        {
            for (final Map.Entry irecipe : this.getRecipes())
            {
                this.arecipes.add(new CachedRocketRecipe(irecipe));
            }
        }
        else
        {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result)
    {
        for (final Map.Entry irecipe : this.getRecipes())
        {
            if (NEIServerUtils.areStacksSameTypeCrafting(((PositionedStack) irecipe.getValue()).item, result))
            {
                this.arecipes.add(new CachedRocketRecipe(irecipe));
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient)
    {
        for (final Map.Entry irecipe : this.getRecipes())
        {
            for (final PositionedStack pstack : (ArrayList<PositionedStack>) irecipe.getKey())
            {
                if (NEIServerUtils.areStacksSameTypeCrafting(ingredient, pstack.item))
                {
                    this.arecipes.add(new CachedRocketRecipe(irecipe));
                    break;
                }
            }
        }
    }

    public class CachedRocketRecipe extends TemplateRecipeHandler.CachedRecipe
    {
        public ArrayList<PositionedStack> input;
        public PositionedStack output;

        @Override
        public ArrayList<PositionedStack> getIngredients()
        {
            return this.input;
        }

        @Override
        public PositionedStack getResult()
        {
            return this.output;
        }

        public CachedRocketRecipe(ArrayList<PositionedStack> pstack1, PositionedStack pstack2)
        {
            super();
            this.input = pstack1;
            this.output = pstack2;
        }

        public CachedRocketRecipe(Map.Entry recipe)
        {
            this((ArrayList<PositionedStack>) recipe.getKey(), (PositionedStack) recipe.getValue());
        }
    }

    @Override
    public String getRecipeName()
    {
        return "NASA Workbench";
    }

    @Override
    public String getGuiTexture()
    {
        return "/mods/galacticraftcore/textures/gui/buggybench.png";
    }
}
