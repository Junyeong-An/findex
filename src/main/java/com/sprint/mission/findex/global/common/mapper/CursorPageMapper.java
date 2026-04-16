package com.sprint.mission.findex.global.common.mapper;

import com.sprint.mission.findex.global.common.dto.CursorPageResponse;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
public class CursorPageMapper {

  /**
   * Slice 기반 매핑. Slice는 count query를 수행하지 않으므로
   * totalElements를 별도 count 쿼리로 계산하여 전달해야 합니다.
   */
  public <T> CursorPageResponse<T> fromSlice(
      Slice<T> slice,
      Function<T, UUID> cursorExtractor,
      Long totalElements
  ) {
    List<T> content = slice.getContent();
    String nextCursor = null;

    if (slice.hasNext() && !content.isEmpty()) {
      UUID lastId = cursorExtractor.apply(content.get(content.size() - 1));
      nextCursor = lastId != null ? lastId.toString() : null;
    }

    return new CursorPageResponse<>(
        content,
        nextCursor,
        nextCursor,
        slice.getSize(),
        totalElements,
        slice.hasNext()
    );
  }

  public <T> CursorPageResponse<T> fromPage(
      Page<T> page,
      Function<T, UUID> cursorExtractor
  ) {
    List<T> content = page.getContent();
    String nextCursor = null;

    if (page.hasNext() && !content.isEmpty()) {
      UUID lastId = cursorExtractor.apply(content.get(content.size() - 1));
      nextCursor = lastId != null ? lastId.toString() : null;
    }

    return new CursorPageResponse<>(
        content,
        nextCursor,
        nextCursor,
        page.getSize(),
        page.getTotalElements(),
        page.hasNext()
    );
  }
}
