package com.tgayle.reddit.models

enum class RedditScope(val redditName: String) {
    Creddits("creddits"),
    Modcontributors("modcontributors"),
    Modmail("modmail"),
    Modconfig("modconfig"),
    Subscribe("subscribe"),
    Structuredstyles("structuredstyles"),
    Vote("vote"),
    Wikiedit("wikiedit"),
    Mysubreddits("mysubreddits"),
    Submit("submit"),
    Modlog("modlog"),
    Modposts("modposts"),
    Modflair("modflair"),
    Save("save"),
    Modothers("modothers"),
    Read("read"),
    Privatemessages("privatemessages"),
    Report("report"),
    Identity("identity"),
    Livemanage("livemanage"),
    Account("account"),
    Modtraffic("modtraffic"),
    Wikiread("wikiread"),
    Edit("edit"),
    Modwiki("modwiki"),
    Modself("modself"),
    History("history"),
    Flair("flair");

    companion object {
        fun allScopes() = values().map { it.redditName }
    }
}