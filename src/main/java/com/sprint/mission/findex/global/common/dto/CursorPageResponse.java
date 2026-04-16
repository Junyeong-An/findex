package com.sprint.mission.findex.global.common.dto;

import java.util.List;

public record CursorPageResponse<T>(
    List<T> content,
    String nextCursor,
    String nextIdAfter,
    int size,
    Long totalElements,
    boolean hasNext
) {

  public static <T> CursorPageResponse<T> of(
      List<T> content,
      String nextCursor,
      String nextIdAfter,
      int size,
      Long totalElements,
      boolean hasNext
  ) {
    return new CursorPageResponse<>(
        content,
        nextCursor,
        nextIdAfter,
        size,
        totalElements,
        hasNext
    );
  }
}
