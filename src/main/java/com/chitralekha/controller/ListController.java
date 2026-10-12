package com.chitralekha.controller;

import com.chitralekha.entity.ContentList;
import com.chitralekha.entity.ContentListItem;
import com.chitralekha.integration.ContentNormalizationService;
import com.chitralekha.service.ListService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lists")
public class ListController {
    private final ListService listService;
    private final ContentNormalizationService normalizationService;

    public ListController(ListService listService, ContentNormalizationService normalizationService) {
        this.listService = listService;
        this.normalizationService = normalizationService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createList(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String description = payload.get("description");
        String visibility = payload.get("visibility");

        ContentList list = listService.createList(name, description, visibility);
        return ResponseEntity.ok(toListMap(list));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getPublicLists() {
        List<ContentList> lists = listService.getPublicLists();
        return ResponseEntity.ok(lists.stream().map(this::toListMap).toList());
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<Map<String, Object>>> getUserLists(@PathVariable String username) {
        List<ContentList> lists = listService.getUserLists(username);
        return ResponseEntity.ok(lists.stream().map(this::toListMap).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getListDetails(@PathVariable Long id) {
        ContentList list = listService.getList(id);
        List<ContentListItem> items = listService.getListItems(id);

        List<Map<String, Object>> itemsList = items.stream().map(item -> Map.<String, Object>of(
                "id", item.getId(),
                "order", item.getItemOrder(),
                "notes", item.getNotes() != null ? item.getNotes() : "",
                "addedAt", item.getAddedAt().toString(),
                "content", normalizationService.toDto(item.getContent())
        )).toList();

        Map<String, Object> res = new java.util.HashMap<>(toListMap(list));
        res.put("items", itemsList);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateList(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String description = payload.get("description");
        String visibility = payload.get("visibility");

        ContentList list = listService.updateList(id, name, description, visibility);
        return ResponseEntity.ok(toListMap(list));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteList(@PathVariable Long id) {
        listService.deleteList(id);
        return ResponseEntity.ok(Map.of("message", "List deleted successfully"));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<Map<String, Object>> addItem(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        Long contentId = ((Number) payload.get("contentId")).longValue();
        String notes = (String) payload.get("notes");

        ContentListItem item = listService.addItem(id, contentId, notes);
        return ResponseEntity.ok(Map.of(
                "id", item.getId(),
                "order", item.getItemOrder(),
                "notes", item.getNotes() != null ? item.getNotes() : "",
                "content", normalizationService.toDto(item.getContent())
        ));
    }

    @DeleteMapping("/{id}/items/{contentId}")
    public ResponseEntity<Map<String, String>> removeItem(
            @PathVariable Long id,
            @PathVariable Long contentId) {
        listService.removeItem(id, contentId);
        return ResponseEntity.ok(Map.of("message", "Item removed from list"));
    }

    private Map<String, Object> toListMap(ContentList list) {
        return Map.of(
                "id", list.getId(),
                "name", list.getName(),
                "description", list.getDescription() != null ? list.getDescription() : "",
                "visibility", list.getVisibility(),
                "createdAt", list.getCreatedAt().toString(),
                "owner", Map.of(
                        "id", list.getOwner().getId(),
                        "username", list.getOwner().getUsername()
                )
        );
    }
}
