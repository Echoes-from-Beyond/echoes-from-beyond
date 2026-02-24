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

package org.echoesfrombeyond.echoesfrombeyond.system;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import java.util.List;
import org.echoesfrombeyond.echoesfrombeyond.asset.SigilPattern;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class EvaluateSpellSystem extends EntityEventSystem<EntityStore, EvaluateSpellSystem.Event> {
  public EvaluateSpellSystem() {
    super(Event.class);
  }

  @Override
  public void handle(
      int i,
      ArchetypeChunk<EntityStore> archetypeChunk,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> commandBuffer,
      Event event) {
    if (event.isCancelled()) return;

    outer:
    for (int j = 0; j < event.patterns.size(); j++) {
      var pattern = event.patterns.get(j);

      if (pattern.isModifier()) {
        var mergedModifier = new ModifierRepr();
        for (int k = j + 1; k < event.patterns.size(); k++) {
          var sample = event.patterns.get(k);
          if (!sample.isModifier()) continue outer;

          for (var entry : sample.getVars().object2FloatEntrySet())
            mergedModifier.vars.merge(entry.getKey(), entry.getFloatValue(), Float::sum);
        }
      }
    }
  }

  @Override
  public Query<EntityStore> getQuery() {
    return Archetype.empty();
  }

  public static class Event extends CancellableEcsEvent {
    public final List<SigilPattern> patterns;

    public Event(List<SigilPattern> patterns) {
      this.patterns = patterns;
    }
  }

  private static class ModifierRepr {
    public final Object2FloatMap<String> vars = new Object2FloatOpenHashMap<>();
  }
}
