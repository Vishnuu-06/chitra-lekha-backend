package com.chitralekha.service;

import com.chitralekha.entity.Content;
import com.chitralekha.entity.ContentList;
import com.chitralekha.entity.ContentListItem;
import com.chitralekha.entity.User;
import com.chitralekha.exception.ResourceNotFoundException;
import com.chitralekha.repository.ContentListItemRepository;
import com.chitralekha.repository.ContentListRepository;
import com.chitralekha.repository.ContentRepository;
import com.chitralekha.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ListService {
    private final ContentListRepository listRepository;
    private final ContentListItemRepository itemRepository;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    public ListService(
            ContentListRepository listRepository,
            ContentListItemRepository itemRepository,
            ContentRepository contentRepository,
            UserRepository userRepository,
            AuthService authService) {
        this.listRepository = listRepository;
        this.itemRepository = itemRepository;
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Transactional
    public ContentList createList(String name, String description, String visibility) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("List title is required");
        }
        User user = authService.getCurrentUser();
        String vis = (visibility != null && visibility.equalsIgnoreCase("PRIVATE")) ? "PRIVATE" : "PUBLIC";
        ContentList list = new ContentList(user, name.trim(), description != null ? description.trim() : "", vis);
        return listRepository.save(list);
    }

    @Transactional
    public ContentList updateList(Long listId, String name, String description, String visibility) {
        User user = authService.getCurrentUser();
        ContentList list = listRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("List not found: " + listId));

        if (!list.getOwner().getId().equals(user.getId())) {
            throw new IllegalStateException("You do not own this list");
        }

        if (name != null && !name.trim().isEmpty()) {
            list.setName(name.trim());
        }
        if (description != null) {
            list.setDescription(description.trim());
        }
        if (visibility != null) {
            list.setVisibility(visibility.equalsIgnoreCase("PRIVATE") ? "PRIVATE" : "PUBLIC");
        }

        return listRepository.save(list);
    }

    @Transactional
    public void deleteList(Long listId) {
        User user = authService.getCurrentUser();
        ContentList list = listRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("List not found: " + listId));

        if (!list.getOwner().getId().equals(user.getId())) {
            throw new IllegalStateException("You do not own this list");
        }
        listRepository.delete(list);
    }

    public ContentList getList(Long listId) {
        ContentList list = listRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("List not found: " + listId));

        if ("PRIVATE".equalsIgnoreCase(list.getVisibility())) {
            Optional<User> currentUserOpt = authService.getAuthenticatedUserOptional();
            if (currentUserOpt.isEmpty() || !currentUserOpt.get().getId().equals(list.getOwner().getId())) {
                throw new IllegalStateException("This list is private");
            }
        }
        return list;
    }

    public List<ContentListItem> getListItems(Long listId) {
        ContentList list = getList(listId);
        return itemRepository.findByListOrderByAddedAtDesc(list);
    }

    @Transactional
    public ContentListItem addItem(Long listId, Long contentId, String notes) {
        User user = authService.getCurrentUser();
        ContentList list = listRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("List not found: " + listId));

        if (!list.getOwner().getId().equals(user.getId())) {
            throw new IllegalStateException("You do not own this list");
        }

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found: " + contentId));

        Optional<ContentListItem> existing = itemRepository.findByListAndContent(list, content);
        if (existing.isPresent()) {
            ContentListItem item = existing.get();
            if (notes != null) item.setNotes(notes.trim());
            return itemRepository.save(item);
        }

        long count = itemRepository.countByList(list);
        ContentListItem item = new ContentListItem(list, content, (int) count + 1, notes != null ? notes.trim() : null);
        return itemRepository.save(item);
    }

    @Transactional
    public void removeItem(Long listId, Long contentId) {
        User user = authService.getCurrentUser();
        ContentList list = listRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("List not found: " + listId));

        if (!list.getOwner().getId().equals(user.getId())) {
            throw new IllegalStateException("You do not own this list");
        }

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found: " + contentId));

        itemRepository.deleteByListAndContent(list, content);
    }

    public List<ContentList> getPublicLists() {
        return listRepository.findByVisibilityOrderByCreatedAtDesc("PUBLIC");
    }

    public List<ContentList> getUserLists(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Optional<User> current = authService.getAuthenticatedUserOptional();
        boolean isOwner = current.isPresent() && current.get().getId().equals(user.getId());

        if (isOwner) {
            return listRepository.findByOwnerOrderByCreatedAtDesc(user);
        }
        return listRepository.findByOwnerAndVisibility(user, "PUBLIC");
    }
}
