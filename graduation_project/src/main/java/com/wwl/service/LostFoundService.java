package com.wwl.service;

import com.wwl.model.dto.LostFoundDTO;
import com.wwl.model.entity.LostFoundApplication;
import com.wwl.model.entity.LostFoundItem;

import java.util.List;
import java.util.Map;

public interface LostFoundService {
    List<LostFoundItem> list(LostFoundDTO dto);

    LostFoundItem get(Long id, String userUid);

    LostFoundItem publish(LostFoundDTO dto);

    LostFoundApplication apply(LostFoundDTO dto);

    boolean approve(LostFoundDTO dto);

    boolean reject(LostFoundDTO dto);

    boolean complete(LostFoundDTO dto);

    Map<String, Object> stats();

    Map<String, Object> notifications(String userUid);

    boolean delete(Long id, String userUid);
}
