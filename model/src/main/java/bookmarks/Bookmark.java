package bookmarks;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Bookmark {

    @Id
    @GeneratedValue
    private Long id;

    @JsonIgnore
    @ManyToOne
    private Account account;

    private String uri;

    private String description;

    private Bookmark() { } // JPA only

    public Bookmark(final Account account, final String uri, final String description) {
        
        this.account = account;
        this.uri = uri;
        this.description = description;
    }

    public static Bookmark from(Account account, Bookmark bookmark) {
        return new Bookmark(account, bookmark.uri, bookmark.getDescription());
    }

    public Long getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public String getUri() {
        return uri;
    }

    public String getDescription() {
        return description;
    }
}
