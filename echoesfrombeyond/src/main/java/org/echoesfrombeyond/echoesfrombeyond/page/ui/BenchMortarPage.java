package org.echoesfrombeyond.echoesfrombeyond.page.ui;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class BenchMortarPage extends InteractiveCustomUIPage<BenchMortarPage.BenchMortarData> {

  public BenchMortarPage(PlayerRef playerRef) {
    super(playerRef, CustomPageLifetime.CanDismiss, BenchMortarData.CODEC);
  }

  @Override
  public void build(Ref<EntityStore> ref, UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, Store<EntityStore> store) {
    commandBuilder.append("BenchMortarPage.ui");
    // TODO: store bench data persistently and populate all the input buttons with it

    for(int i = 0; i < 5; i++) {
      // TODO: currently proof of concept for display; rework this to pull from the STORAGE SYSTEM
      String sharedSelect = "#SharedInventory[" + i + "]";

      commandBuilder.append("#SharedInventory", "ItemSlotPreset.ui");
    }

    for(int i = 0; i < 5; i++) {
      // TODO: currently proof of concept for display; rework this to pull from the PLAYER INVENTORY
      String invSelect = "#PlayerInventory[" + i + "]";

      commandBuilder.append("#PlayerInventory", "ItemSlotPreset.ui");
    }

    for(int i = 0; i < 5; i++) {
      // TODO: currently proof of concept for display; rework this to pull from the OVERFLOW
      String overflowSelect = "#Overflow[" + i + "]";

      commandBuilder.append("#Overflow", "ItemSlotPreset.ui");
    }
  }

  @Override
  public void handleDataEvent(
          Ref<EntityStore> ref, Store<EntityStore> store, BenchMortarData data) {}

  @NullMarked
  @ModelBuilder
  public static class BenchMortarData {
    public static final BuilderCodec<BenchMortarData> CODEC =
            CodecUtil.modelBuilder(
                    BenchMortarData.class,
                    Plugin.getSharedResolver());

    public BenchMortarData() {
      ClickedSlot = "invalid";
    }

    public String ClickedSlot;

    public String getClickedSlot() {
      return ClickedSlot;
    }
  }
}
