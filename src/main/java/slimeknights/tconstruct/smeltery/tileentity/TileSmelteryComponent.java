package slimeknights.tconstruct.smeltery.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import slimeknights.mantle.multiblock.MultiServantLogic;

public class TileSmelteryComponent extends MultiServantLogic {

  // we send all our info to the client on load
  @Override
  public NBTTagCompound getUpdateTag() {
    NBTTagCompound tag = new NBTTagCompound();
    writeToNBT(tag);
    return tag;
  }

  @Override
  public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
    super.onDataPacket(net, pkt);
    readFromNBT(pkt.getNbtCompound());
  }

  protected TileSmeltery getSmeltery() {
    if(getHasMaster()) {
      TileEntity te = worldObj.getTileEntity(getMasterPosition());
      if(te instanceof TileSmeltery) {
        return (TileSmeltery) te;
      }
    }
    return null;
  }
}
