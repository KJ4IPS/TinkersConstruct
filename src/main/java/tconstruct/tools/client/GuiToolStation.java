package tconstruct.tools.client;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.util.Point;

import tconstruct.common.client.gui.GuiElement;
import tconstruct.common.client.gui.GuiModule;
import tconstruct.common.inventory.ContainerMultiModule;
import tconstruct.library.Util;
import tconstruct.library.client.ToolBuildGuiInfo;
import tconstruct.tools.client.module.GuiButtonsToolStation;
import tconstruct.tools.client.module.GuiSideButtons;
import tconstruct.tools.tileentity.TileToolStation;

@SideOnly(Side.CLIENT)
public class GuiToolStation extends GuiTinkerStation {

  private static final ResourceLocation BACKGROUND = Util.getResource("textures/gui/toolstation.png");

  private static final GuiElement ItemCover = new GuiElement(176, 18, 80, 64, 256, 256);
  private static final GuiElement SlotBackground = new GuiElement(176, 0, 18, 18);
  private static final GuiElement SlotBorder = new GuiElement(194, 0, 18, 18);

  private static final int Table_slot_count = 6;

  protected GuiSideButtons buttons;
  protected int activeSlots; // how many of the available slots are active
  protected ToolBuildGuiInfo currentInfo;

  public GuiToolStation(InventoryPlayer playerInv, World world, BlockPos pos, TileToolStation tile) {
    super(world, pos, (ContainerMultiModule) tile.createContainer(playerInv, world, pos));

    buttons = new GuiButtonsToolStation(this, inventorySlots);
    this.addModule(buttons);

    this.ySize = 174;
  }

  @Override
  public void initGui() {
    super.initGui();

    // workaround to line up the tabs on switching even though the GUI is a tad higher
    this.guiTop += 4;
    this.cornerY += 4;

    for(GuiModule module : modules) {
      module.guiTop += 4;
    }
  }

  public void onToolSelection(ToolBuildGuiInfo info) {
    activeSlots = Math.min(info.positions.size(), Table_slot_count);
    currentInfo = info;

    int i;
    for(i = 0; i < activeSlots; i++) {
      Point point = info.positions.get(i);

      Slot slot = inventorySlots.getSlot(i);
      slot.xDisplayPosition = point.getX();
      slot.yDisplayPosition = point.getY();
    }

    // remaining slots
    int stillFilled = 0;
    for(; i < Table_slot_count; i++) {
      Slot slot = inventorySlots.getSlot(i);
      if(slot.getHasStack()) {
        slot.xDisplayPosition = 87 + 20 * stillFilled;
        slot.yDisplayPosition = 62;
        stillFilled++;
      }
      else {
        // todo: slot.disable
        slot.xDisplayPosition = 0;
        slot.yDisplayPosition = 0;
      }
    }
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    drawBackground(BACKGROUND);

    int xOff = 0;
    int yOff = 0;

    int x = 0;
    int y = 0;

    // the slot backgrounds
    for(int i = 0; i < activeSlots; i++) {
      Slot slot = inventorySlots.getSlot(i);
      SlotBackground.draw(x + this.cornerX + slot.xDisplayPosition - 1, y + this.cornerY + slot.yDisplayPosition - 1);
    }


    // draw the item background
    final float scale = 4.0f;
    GlStateManager.scale(scale, scale, 1.0f);
    //renderItemIntoGuiBackground(back, (this.cornerX + 15) / 4 + xOff, (this.cornerY + 18) / 4 + yOff);
    {
      int logoX = (this.cornerX + 10) / 4 + xOff;
      int logoY = (this.cornerY + 18) / 4 + yOff;

      if(currentInfo != null) {
        if(currentInfo.tool != null) {
          itemRender.renderItemIntoGUI(currentInfo.tool, logoX, logoY);
        }
        else if(currentInfo == GuiButtonRepair.info) {
          this.mc.getTextureManager().bindTexture(Util.getResource("textures/gui/icons.png"));
          ICON_Anvil.draw(logoX, logoY);
        }
      }
    }
    GlStateManager.scale(1f / scale, 1f / scale, 1.0f);

    // rebind gui texture
    this.mc.getTextureManager().bindTexture(BACKGROUND);

    // reset state after item drawing
    GlStateManager.enableBlend();
    GlStateManager.enableAlpha();
    RenderHelper.disableStandardItemLighting();
    GlStateManager.disableDepth();

    // draw the halftransparent "cover" over the item
    GlStateManager.color(1.0f, 1.0f, 1.0f, 0.7f);
    ItemCover.draw(this.cornerX + 7, this.cornerY + 18);

    // full opaque. Draw the borders of the slots
    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    for(int i = 0; i < activeSlots; i++) {
      Slot slot = inventorySlots.getSlot(i);
      SlotBorder.draw(
          x + this.cornerX + slot.xDisplayPosition - 1, y + this.cornerY + slot.yDisplayPosition - 1);
    }

    this.mc.getTextureManager().bindTexture(Util.getResource("textures/gui/icons.png"));

    // slot logos
    for(int i = 0; i < activeSlots; i++) {
      Slot slot = inventorySlots.getSlot(i);
      if(currentInfo == GuiButtonRepair.info) {
        GuiElement icon = null;
        
        if(i == 0) {
          icon = ICON_Pickaxe;
        }
        else if(i == 1) {
          icon = ICON_Dust;
        }
        else if(i == 2) {
          icon = ICON_Lapis;
        }
        else if(i == 3) {
          icon = ICON_Ingot;
        }
        else if(i == 4) {
          icon = ICON_Gem;
        }
        else if(i == 5) {
          icon = ICON_Quartz;
        }

        if(icon != null) {
          icon.draw(x + this.cornerX + slot.xDisplayPosition - 1, y + this.cornerY + slot.yDisplayPosition - 1);
        }
      }
    }

    // continue as usual and hope that the drawing state is not completely wrecked
    super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
  }
}
