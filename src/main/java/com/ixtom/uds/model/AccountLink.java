package com.ixtom.uds.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "AccountLink",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "provider" }),
    }
)
public class AccountLink {

    @EmbeddedId
    private AccountLinkId id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAuth user;

    public AccountLink() {}

    public AccountLink(String provider, String externalId, UserAuth user) {
        this.id = new AccountLinkId(provider, externalId);
        this.user = user;
    }

    public AccountLinkId getId() {
		return id;
	}

	public void setId(AccountLinkId id) {
		this.id = id;
	}

	public UserAuth getUser() {
		return user;
	}

	public void setUser(UserAuth user) {
		this.user = user;
	}

	@Embeddable
    public static class AccountLinkId implements Serializable {

        @Column(nullable = false)
        private String provider;

        @Column(nullable = false)
        private String externalId;

        public AccountLinkId() {}

        public AccountLinkId(String provider, String externalId) {
            this.provider = provider;
            this.externalId = externalId;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;

            AccountLinkId other = (AccountLinkId) obj;

            return  Objects.equals(provider, other.provider) && Objects.equals(externalId, other.externalId);
        }

        @Override
        public int hashCode() {
            int hash = Objects.hashCode(provider);
            hash = hash * 31 + Objects.hashCode(externalId);
            return hash;
        }

		public String getProvider() {
			return provider;
		}

		public void setProvider(String provider) {
			this.provider = provider;
		}

		public String getExternalId() {
			return externalId;
		}

		public void setExternalId(String externalId) {
			this.externalId = externalId;
		}
    }
}
