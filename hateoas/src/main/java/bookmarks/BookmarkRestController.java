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

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

// tag::code[]
@RestController
@RequestMapping("/{userId}/bookmarks")
class BookmarkRestController {

	private final BookmarkRepository bookmarkRepository;

	private final AccountRepository accountRepository;

    BookmarkRestController(BookmarkRepository bookmarkRepository,
                           AccountRepository accountRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.accountRepository = accountRepository;
    }

    @RequestMapping(method = RequestMethod.GET)
	Resources<BookmarkResource> readBookmarks(@PathVariable String userId) {

		this.validateUser(userId);

		List<BookmarkResource> bookmarkResourceList = bookmarkRepository
				.findByAccountUsername(userId).stream().map(BookmarkResource::new)
				.collect(Collectors.toList());

		return new Resources<>(bookmarkResourceList);
	}

	@RequestMapping(method = RequestMethod.POST)
	ResponseEntity<?> add(@PathVariable String userId, @RequestBody Bookmark input) {

		this.validateUser(userId);

		return accountRepository.findByUsername(userId)
            .map(account -> {
                Bookmark bookmark = bookmarkRepository
                        .save(new Bookmark(account, input.getUri(), input.getDescription()));

                Link forOneBookmark = new BookmarkResource(bookmark).getLink("self");

                return ResponseEntity.created(URI.create(forOneBookmark.getHref())).build();
            })
            .orElse(ResponseEntity.noContent().build());
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{bookmarkId}")
	BookmarkResource readBookmark(@PathVariable String userId,
                                  @PathVariable Long bookmarkId) {
		this.validateUser(userId);
		return new BookmarkResource(this.bookmarkRepository.findOne(bookmarkId));
	}

	private void validateUser(String userId) {
		this.accountRepository
			.findByUsername(userId)
			.orElseThrow(() -> new UserNotFoundException(userId));
	}
}
// end::code[]