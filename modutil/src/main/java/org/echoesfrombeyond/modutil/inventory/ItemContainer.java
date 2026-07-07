package org.echoesfrombeyond.modutil.inventory;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public interface ItemContainer {
  @FunctionalInterface
  interface StackConsumer {
    boolean accept(int slot, ItemStack stack);
  }

  enum AddResult {
    SUCCESS,
    INVENTORY_FROZEN,
    TARGET_FULL,
    INVALID_ITEM;

    public boolean isSuccess() {
      return this == SUCCESS;
    }
  }

  record SetResult(AddResult result, @Nullable ItemStack oldItem) {
    public SetResult {
      if (!result.isSuccess() && oldItem != null) throw new IllegalArgumentException();
    }

    public boolean isSuccess() {
      return result.isSuccess();
    }
  }

  int getItemCount();

  int getItemCapacity();

  AddResult addItem(ItemStack stack);

  AddResult addItem(ItemStack stack, int index);

  SetResult setItem(ItemStack stack, int index);

  @Nullable ItemStack removeItem(int index);

  List<ItemStack> removeAllItems();

  void forEachItem(StackConsumer body);
}
