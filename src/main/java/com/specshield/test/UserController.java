package com.specshield.test;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

/**
 * Minimal in-memory implementation of the v1 OpenAPI spec for this test
 * fixture. Exists so the project is a believable Spring Boot service that
 * a real customer might run `specshield init` against — the spec files
 * (not this controller) are what SpecShield actually consumes.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<String, User> store = new LinkedHashMap<>();

    public UserController() {
        store.put("11111111-1111-1111-1111-111111111111",
                new User("11111111-1111-1111-1111-111111111111", "Aarav", "aarav@example.com", 1, Instant.parse("2026-01-01T00:00:00Z")));
        store.put("22222222-2222-2222-2222-222222222222",
                new User("22222222-2222-2222-2222-222222222222", "Priya", null, 2, Instant.parse("2026-02-01T00:00:00Z")));
    }

    @GetMapping
    public Map<String, Object> list(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0")  int offset) {
        List<User> all = new ArrayList<>(store.values());
        int end = Math.min(offset + limit, all.size());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("data",  all.subList(Math.min(offset, all.size()), end));
        body.put("total", all.size());
        return body;
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody Map<String, Object> req) {
        String name = (String) req.get("name");
        if (name == null) return ResponseEntity.badRequest().build();
        String email = (String) req.get("email");
        String id = UUID.randomUUID().toString();
        User u = new User(id, name, email, store.size() + 1, Instant.now());
        store.put(id, u);
        return ResponseEntity.status(201).body(u);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> get(@PathVariable String id) {
        User u = store.get(id);
        return u == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(u);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<User> patch(@PathVariable String id, @RequestBody Map<String, Object> patch) {
        User u = store.get(id);
        if (u == null) return ResponseEntity.notFound().build();
        if (patch.containsKey("name"))  u.setName((String)  patch.get("name"));
        if (patch.containsKey("email")) u.setEmail((String) patch.get("email"));
        return ResponseEntity.ok(u);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        store.remove(id);
        return ResponseEntity.noContent().build();
    }

    public static class User {
        private String id;
        private String name;
        private String email;
        private Integer legacy_id;
        private Instant created_at;

        public User(String id, String name, String email, Integer legacyId, Instant createdAt) {
            this.id = id; this.name = name; this.email = email;
            this.legacy_id = legacyId; this.created_at = createdAt;
        }
        public String  getId()         { return id; }
        public String  getName()       { return name; }
        public String  getEmail()      { return email; }
        public Integer getLegacy_id()  { return legacy_id; }
        public Instant getCreated_at() { return created_at; }
        public void    setName(String name)   { this.name = name; }
        public void    setEmail(String email) { this.email = email; }
    }
}
