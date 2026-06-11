package org.echoesfrombeyond.echoesfrombeyond.page.supplier;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.Plugin;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.echoesfrombeyond.page.ui.BenchMortarPage;
import org.jspecify.annotations.NullMarked;

@NullMarked
@ModelBuilder
public class BenchMortarSupplier implements OpenCustomUIInteraction.CustomPageSupplier {
  public static final BuilderCodec<BenchMortarSupplier> CODEC =
          CodecUtil.modelBuilder(
                  BenchMortarSupplier.class,
                  Plugin.getSharedResolver());

  public CustomUIPage tryCreate(Ref<EntityStore> ref, ComponentAccessor<EntityStore> componentAccessor, PlayerRef playerRef, InteractionContext context) {
    return new BenchMortarPage(playerRef);
  }
}
