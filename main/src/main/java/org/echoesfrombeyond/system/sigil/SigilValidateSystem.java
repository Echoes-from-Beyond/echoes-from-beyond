/*
 * Echoes from Beyond: Hytale Mod
 * Copyright (C) 2025 Echoes from Beyond Team <chemky2000@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.echoesfrombeyond.system.sigil;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import org.echoesfrombeyond.codec.SigilPoint;
import org.echoesfrombeyond.sigil.SigilValidation;
import org.echoesfrombeyond.util.thread.Once;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class SigilValidateSystem extends EntityEventSystem<EntityStore, SigilValidateSystem.Event> {
  private final Once<Archetype<EntityStore>> archetype;

  public SigilValidateSystem() {
    super(Event.class);
    this.archetype = Once.of(Archetype::empty);
  }

  @Override
  public void handle(
      int i,
      ArchetypeChunk<EntityStore> archetypeChunk,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> commandBuffer,
      Event event) {
    if (event.isCancelled()) return;

    var encoded = SigilPoint.encodeArray(event.points.toArray(SigilPoint[]::new));
    var optional = SigilValidation.canonicalize(encoded);

    if (optional.isEmpty()) return;

    var ref = archetypeChunk.getReferenceTo(i);
    commandBuffer.invoke(ref, new SigilQueueSystem.Event(optional.get()));
  }

  @Override
  public Query<EntityStore> getQuery() {
    return archetype.get();
  }

  /**
   * Event invoked when the player first draws a Sigil. It isn't necessarily valid or even
   * canonical.
   */
  public static class Event extends CancellableEcsEvent {
    /** Mutable list of points in the Sigil. Must not contain null elements. */
    public final List<SigilPoint> points;

    public Event(List<SigilPoint> points) {
      this.points = points;
    }
  }
}
