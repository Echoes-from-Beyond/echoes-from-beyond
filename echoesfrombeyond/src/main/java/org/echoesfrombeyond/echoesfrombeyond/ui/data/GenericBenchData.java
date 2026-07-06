package org.echoesfrombeyond.echoesfrombeyond.ui.data;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import org.echoesfrombeyond.codechelper.CodecUtil;
import org.echoesfrombeyond.codechelper.annotation.ModelBuilder;
import org.echoesfrombeyond.echoesfrombeyond.EchoesFromBeyond;
import org.echoesfrombeyond.echoesfrombeyond.ui.page.BenchStoragePage;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@NullMarked
@ModelBuilder
public class GenericBenchData {
  public static final BuilderCodec<GenericBenchData> CODEC =
          CodecUtil.modelBuilder(GenericBenchData.class, EchoesFromBeyond.get().getResolver());

  @SuppressWarnings({"FieldMayBeFinal", "unused"})
  private @Nullable String Index;

  public @Nullable GenericBenchInteractionType Type;

  @FunctionalInterface
  public interface IndexOp<T extends @Nullable Object, R> {
    R index(int index, T value);
  }

  public <T extends @Nullable Object, R> Optional<R> useIndex(
          List<T> indexable, GenericBenchData.IndexOp<? super T, ? extends R> callback) {
    var index = Index;
    if (index == null) return Optional.empty();

    int actualIndex;
    try {
      actualIndex = Integer.parseInt(index);
      if (actualIndex < 0 || actualIndex >= indexable.size()) return Optional.empty();
    } catch (NumberFormatException _) {
      return Optional.empty();
    }

    return Optional.of(callback.index(actualIndex, indexable.get(actualIndex)));
  }
}
