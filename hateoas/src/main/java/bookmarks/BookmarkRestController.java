/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bookmarks;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import java.net.URI;
import java.util.stream.Collectors;

import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// tag::code[]
@RestController
class BookmarkRestController {

	private final BookmarkRepository bookmarkRepository;
	private final AccountRepository accountRepository;

	BookmarkRestController(BookmarkRepository bookmarkRepository,
						   AccountRepository accountRepository) {
		this.bookmarkRepository = bookmarkRepository;
		this.accountRepository = accountRepository;
	}

	@GetMapping
	ResourceSupport root() {
		ResourceSupport root = new ResourceSupport();

		root.add(accountRepository.findAll().stream()
			.map(account -> linkTo(methodOn(BookmarkRestController.class)
				.readBookmarks(account.getUsername()))
				.withRel(account.getUsername()))
			.collect(Collectors.toList()));

		return root;
	}

	@GetMapping("/{userId}/bookmarks")
	Resources<BookmarkResource> readBookmarks(@PathVariable String userId) {

		this.validateUser(userId);

		return new Resources<>(bookmarkRepository
			.findByAccountUsername(userId).stream()
			.map(BookmarkResource::new)
			.collect(Collectors.toList()));
	}

	@PostMapping("/{userId}/bookmarks")
	ResponseEntity<?> add(@PathVariable String userId, @RequestBody Bookmark input) {

		this.validateUser(userId);

		return accountRepository.findByUsername(userId)
			.map(account -> ResponseEntity
				.created(
					URI.create(
						new BookmarkResource(
							bookmarkRepository.save(Bookmark.from(account, input)))
						.getLink("self").getHref()))
				.build())
			.orElse(ResponseEntity.noContent().build());
	}

	/**
	 * Find a single bookmark and transform it into a {@link BookmarkResource}.
	 * 
	 * @param userId
	 * @param bookmarkId
	 * @return
	 */
	@GetMapping("/{userId}/bookmarks/{bookmarkId}")
	BookmarkResource readBookmark(@PathVariable String userId,
								  @PathVariable Long bookmarkId) {
		this.validateUser(userId);

		return this.bookmarkRepository.findById(bookmarkId)
			.map(BookmarkResource::new)
			.orElseThrow(() -> new BookmarkNotFoundException(bookmarkId));
	}

	/**
	 * Verify the {@literal userId} exists.
	 * 
	 * @param userId
	 */
	private void validateUser(String userId) {
		this.accountRepository
			.findByUsername(userId)
			.orElseThrow(() -> new UserNotFoundException(userId));
	}
}
// end::code[]