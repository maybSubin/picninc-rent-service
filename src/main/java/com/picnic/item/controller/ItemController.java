package com.picnic.item.controller;

import com.picnic.item.domain.ItemType;
import com.picnic.item.dto.ItemAvailabilityResponse;
import com.picnic.item.dto.ItemCreateRequest;
import com.picnic.item.dto.ItemResponse;
import com.picnic.item.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody ItemCreateRequest request) {
        ItemResponse response = itemService.create(request);
        return ResponseEntity.created(URI.create("/api/items/" + response.id())).body(response);
    }

    @GetMapping
    public List<ItemResponse> findAll(@RequestParam(required = false) ItemType type) {
        return itemService.findAll(type);
    }

    @GetMapping("/{itemId}")
    public ItemResponse findById(@PathVariable Long itemId) {
        return itemService.findById(itemId);
    }

    @GetMapping("/availability")
    public List<ItemAvailabilityResponse> findAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) ItemType type) {
        return itemService.findAvailability(date, type);
    }
}
