package com.ixtom.uds.dto;

public record DiscordUserDataResponse (
    long id,
    String email,
    String username,

    String avatar,
    Boolean verified
) implements OAuthUserInfo {

	@Override
	public String externalId() {
		return String.valueOf(id);
	}

	@Override
	public String pfpUrl() {
	if (avatar == null) {
	    return null;
	}
	    return "https://cdn.discordapp.com/avatars/" + id + "/" + avatar + ".png";
	}
}
