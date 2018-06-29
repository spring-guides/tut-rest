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

import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// tag::code[]
@RestController
@RequestMapping("/bookmarks")
class BookmarkRestController {

	private final BookmarkRepository bookmarkRepository;
	private final AccountRepository accountRepository;

	BookmarkRestController(BookmarkRepository bookmarkRepository,
						   AccountRepository accountRepository) {
		this.bookmarkRepository = bookmarkRepository;
		this.accountRepository = accountRepository;
	}

	/**
	 * Serve up a collection of links at the root URI for the client to consume.
	 * @return
	 */
	@GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
	ResourceSupport root() {
		ResourceSupport root = new ResourceSupport();

		root.add(this.accountRepository.findAll().stream()
			.map(account -> linkTo(methodOn(BookmarkRestController.class)
				.readBookmarks(account.getUsername()))
				.withRel(account.getUsername()))
			.collect(Collectors.toList()));

		return root;
	}

	/**
	 * Look up a collection of {@link Bookmark}s and transform then into a set of {@link Resources}.
	 * 
	 * @param userId
	 * @return
	 */
	@GetMapping(value = "/{userId}", produces = MediaTypes.HAL_JSON_VALUE)
	Resources<Resource<Bookmark>> readBookmarks(@PathVariable String userId) {

		this.validateUser(userId);

		return new Resources<>(this.bookmarkRepository
			.findByAccountUsername(userId).stream()
			.map(bookmark -> toResource(bookmark, userId))
			.collect(Collectors.toList()));
	}

	@PostMapping("/{userId}")
	ResponseEntity<?> add(@PathVariable String userId, @RequestBody Bookmark input) {

		this.validateUser(userId);

		return this.accountRepository.findByUsername(userId)
			.map(account -> ResponseEntity.created(
					URI.create(
						toResource(
							this.bookmarkRepository.save(Bookmark.from(account, input)), userId)
								.getLink(Link.REL_SELF).getHref()))
				.build())
			.orElse(ResponseEntity.noContent().build());
	}

	/**
	 * Find a single bookmark and transform it into a {@link Resource} of {@link Bookmark}s.
	 * 
	 * @param userId
	 * @param bookmarkId
	 * @return
	 */
	@GetMapping(value = "/{userId}/{bookmarkId}", produces = MediaTypes.HAL_JSON_VALUE)
	Resource<Bookmark> readBookmark(@PathVariable String userId,
								  @PathVariable Long bookmarkId) {
		this.validateUser(userId);

		return this.bookmarkRepository.findById(bookmarkId)
			.map(bookmark -> toResource(bookmark, userId))
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

	/**
	 * Transform a {@link Bookmark} into a {@link Resource}.
	 * 
	 * @param bookmark
	 * @param userId
	 * @return
	 */
	private static Resource<Bookmark> toResource(Bookmark bookmark, String userId) {
		return new Resource(bookmark,

			// Create a raw link using a URI and a rel
			new Link(bookmark.getUri(), "bookmark-uri"),

			// Create a link to a the collection of bookmarks associated with the user
			linkTo(methodOn(BookmarkRestController.class).readBookmarks(userId)).withRel("bookmarks"),

			// Create a "self" link to a single bookmark
			linkTo(methodOn(BookmarkRestController.class).readBookmark(userId, bookmark.getId())).withSelfRel());
	}
}
// end::code[]