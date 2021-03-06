package slimeknights.tconstruct.tools.client;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import java.util.List;

import slimeknights.tconstruct.library.tools.IAoeTool;

@SideOnly(Side.CLIENT)
public class RenderEvents implements IResourceManagerReloadListener {

  private static final ResourceLocation widgetsTexPath = new ResourceLocation("textures/gui/widgets.png"); // GuiIngame.widgetsTexPath
  private final TextureAtlasSprite[] destroyBlockIcons = new TextureAtlasSprite[10];

  @SubscribeEvent
  public void renderExtraBlockBreak(RenderWorldLastEvent event) {
    PlayerControllerMP controllerMP = Minecraft.getMinecraft().playerController;
    EntityPlayer player = Minecraft.getMinecraft().thePlayer;
    World world = player.worldObj;
    // AOE preview
    if(player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof IAoeTool) {
      RayTraceResult mop = player.rayTrace(controllerMP.getBlockReachDistance(), event.getPartialTicks());
      if(mop != null) {
        ItemStack stack = player.getHeldItemMainhand();
        ImmutableList<BlockPos> extraBlocks = ((IAoeTool) stack.getItem()).getAOEBlocks(stack, world, player, mop
            .getBlockPos());
        for(BlockPos pos : extraBlocks) {
          event.getContext().drawSelectionBox(player, new RayTraceResult(new Vec3d(0, 0, 0), null, pos), 0, event
              .getPartialTicks());
        }
      }
    }

    // extra-blockbreak animation
    if(controllerMP.isHittingBlock) {
      if(controllerMP.currentItemHittingBlock != null &&
         controllerMP.currentItemHittingBlock.getItem() instanceof IAoeTool &&
         ((IAoeTool) controllerMP.currentItemHittingBlock.getItem()).isAoeHarvestTool()) {
        ItemStack stack = controllerMP.currentItemHittingBlock;
        BlockPos pos = controllerMP.currentBlock;
        drawBlockDamageTexture(Tessellator.getInstance(),
                               Tessellator.getInstance().getBuffer(),
                               player,
                               event.getPartialTicks(),
                               world,
                               ((IAoeTool) stack.getItem()).getAOEBlocks(stack, world, player, pos));
      }
    }
  }

  // RenderGlobal.drawBlockDamageTexture
  public void drawBlockDamageTexture(Tessellator tessellatorIn, VertexBuffer vertexBuffer, Entity entityIn, float partialTicks, World world, List<BlockPos> blocks)
  {
    double d0 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
    double d1 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
    double d2 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;

    TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;
    int progress = (int)(Minecraft.getMinecraft().playerController.curBlockDamageMP*10f) - 1; // 0-10

    if(progress < 0)
      return;

      renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
      //preRenderDamagedBlocks BEGIN
      GlStateManager.tryBlendFuncSeparate(774, 768, 1, 0);
      GlStateManager.enableBlend();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
      GlStateManager.doPolygonOffset(-3.0F, -3.0F);
      GlStateManager.enablePolygonOffset();
      GlStateManager.alphaFunc(516, 0.1F);
      GlStateManager.enableAlpha();
      GlStateManager.pushMatrix();
      //preRenderDamagedBlocks END

      vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
      vertexBuffer.setTranslation(-d0, -d1, -d2);
      vertexBuffer.noColor();

      for(BlockPos blockpos : blocks) {
        double d3 = (double)blockpos.getX() - d0;
        double d4 = (double)blockpos.getY() - d1;
        double d5 = (double)blockpos.getZ() - d2;
        Block block = world.getBlockState(blockpos).getBlock();
        TileEntity te = world.getTileEntity(blockpos);
        boolean hasBreak = block instanceof BlockChest || block instanceof BlockEnderChest
                           || block instanceof BlockSign || block instanceof BlockSkull;
        if (!hasBreak) hasBreak = te != null && te.canRenderBreaking();

        if (!hasBreak)
        {
            IBlockState iblockstate = world.getBlockState(blockpos);

            if (iblockstate.getBlock().getMaterial(iblockstate) != Material.AIR)
            {
              TextureAtlasSprite textureatlassprite = this.destroyBlockIcons[progress];
              BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
              blockrendererdispatcher.renderBlockDamage(iblockstate, blockpos, textureatlassprite, world);
            }
        }
      }

      tessellatorIn.draw();
      vertexBuffer.setTranslation(0.0D, 0.0D, 0.0D);
      // postRenderDamagedBlocks BEGIN
      GlStateManager.disableAlpha();
      GlStateManager.doPolygonOffset(0.0F, 0.0F);
      GlStateManager.disablePolygonOffset();
      GlStateManager.enableAlpha();
      GlStateManager.depthMask(true);
      GlStateManager.popMatrix();
      // postRenderDamagedBlocks END
  }

  @Override
  public void onResourceManagerReload(IResourceManager resourceManager) {
    TextureMap texturemap = Minecraft.getMinecraft().getTextureMapBlocks();

    for (int i = 0; i < this.destroyBlockIcons.length; ++i)
    {
      this.destroyBlockIcons[i] = texturemap.getAtlasSprite("minecraft:blocks/destroy_stage_" + i);
    }
  }
}
