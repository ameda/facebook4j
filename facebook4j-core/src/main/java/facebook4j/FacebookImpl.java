/*
 * Copyright 2012 Ryuji Yamashita
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

package facebook4j;

import static facebook4j.internal.util.z_F4JInternalParseUtil.*;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import facebook4j.Question.Option;
import facebook4j.auth.Authorization;
import facebook4j.conf.Configuration;
import facebook4j.internal.http.HttpParameter;
import facebook4j.internal.http.HttpResponse;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;
import facebook4j.internal.util.z_F4JInternalStringUtil;

/**
 * A java representation of the <a href="https://developers.facebook.com/docs/reference/api/">Facebook Graph API</a><br>
 * This class is thread safe and can be cached/re-used and used concurrently.<br>
 * Currently this class is not carefully designed to be extended. It is suggested to extend this class only for mock testing purpose.<br>
 *
 * @author Ryuji Yamashita - roundrop at gmail.com
 */
class FacebookImpl extends FacebookBaseImpl implements Facebook {
    private static final long serialVersionUID = 6277119018105563020L;

    /*package*/
    FacebookImpl(Configuration conf, Authorization auth) {
        super(conf, auth);
    }

    private String buildURL(String id) {
        return buildURL(id, null, null);
    }
    private String buildURL(String id, Reading reading) {
        return buildURL(id, null, reading);
    }
    private String buildURL(String id, String connection) {
        return buildURL(id, connection, null);
    }
    private String buildURL(String id, String connection, Reading reading) {
        StringBuilder url = new StringBuilder()
                            .append(conf.getRestBaseURL() + id)
                            .append(connection == null ? "" : "/" + connection)
                            .append(reading == null ? "" : "?" + reading.getQuery());
        return url.toString();
    }
    
    private String buildVideoURL(String id, String connection) {
        return buildVideoURL(id, connection, null);
    }
    private String buildVideoURL(String id, String connection, Reading reading) {
        StringBuilder url = new StringBuilder()
                            .append(conf.getVideoBaseURL() + id)
                            .append(connection == null ? "" : "/" + connection)
                            .append(reading == null ? "" : "?" + reading.getQuery());
        return url.toString();
    }

    private String buildSearchURL(String query, String objectType, Reading reading) {
        String q = null;
        if (query != null) {
            q = HttpParameter.encode(query);
        }
        StringBuilder url = new StringBuilder()
                            .append(buildURL("search"))
                            .append(objectType == null ? "" : "?type=" + objectType)
                            .append(q == null ? "" : objectType == null ? "?q=" + q : "&q=" + q)
                            .append(reading == null ? "" : "&" + reading.getQuery());
        return url.toString();
    }

    /* User Methods */
    
    public User getMe() throws FacebookException {
        return getMe(null);
    }
    public User getMe(Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        User user = factory.createUser(get(buildURL("me", reading)));
        return user;
    }
    
    public User getUser(String userId) throws FacebookException {
        return getUser(userId, null);
    }
    public User getUser(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        User user = factory.createUser(get(buildURL(userId, reading)));
        return user;
    }
    
    public URL getPictureURL() throws FacebookException {
        ensureAuthorizationEnabled();
        return getPictureURL("me");
    }
    public URL getPictureURL(PictureSize size) throws FacebookException {
        ensureAuthorizationEnabled();
        return getPictureURL("me", size);
    }
    public URL getPictureURL(String userId) throws FacebookException {
        return getPictureURL(userId, null);
    }
    public URL getPictureURL(String userId, PictureSize size) throws FacebookException {
        String url = buildURL(userId, "picture");
        HttpResponse res;
        if (size != null) {
            res = get(url, new HttpParameter[]{new HttpParameter("type", size.toString())});
        } else {
            res = get(url);
        }
        try {
            return new URL(res.getResponseHeader("Location"));
        } catch (MalformedURLException urle) {
            throw new FacebookException(urle.getMessage(), urle);
        }
    }
    
    public List<User> getUsers(String... ids) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createUserArray(get(conf.getRestBaseURL(), new HttpParameter[] {
                                        new HttpParameter("ids", z_F4JInternalStringUtil.join(ids))}));
    }
    
    /* Account Methods */

    public ResponseList<Account> getAccounts() throws FacebookException {
        return getAccounts("me", null);
    }
    public ResponseList<Account> getAccounts(Reading reading) throws FacebookException {
        return getAccounts("me", reading);
    }
    public ResponseList<Account> getAccounts(String userId) throws FacebookException {
        return getAccounts(userId, null);
    }
    public ResponseList<Account> getAccounts(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createAccountList(get(buildURL(userId, "accounts", reading)));
    }

    /* Achievement Methods */
    
    public ResponseList<Achievement> getAchievements() throws FacebookException {
        return getAchievements("me", null);
    }
    public ResponseList<Achievement> getAchievements(Reading reading) throws FacebookException {
        return getAchievements("me", reading);
    }
    public ResponseList<Achievement> getAchievements(String userId) throws FacebookException {
        return getAchievements(userId, null);
    }
    public ResponseList<Achievement> getAchievements(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createAchievementList(get(buildURL(userId, "achievements", reading)));
    }

    public String postAchievement(URL achievementURL) throws FacebookException {
        return postAchievement("me", achievementURL);
    }
    public String postAchievement(String userId, URL achievementURL) throws FacebookException {
        ensureAuthorizationEnabled();
        JSONObject json = post(buildURL(userId, "achievements"),
                            new HttpParameter[] {new HttpParameter("achievement", achievementURL.toString())}
                          ).asJSONObject();
        try {
            return json.getString("id");
        } catch (JSONException jsone) {
            throw new FacebookException(jsone.getMessage(), jsone);
        }
    }

    public boolean deleteAchievement(URL achievementURL) throws FacebookException {
        return deleteAchievement("me", achievementURL);
    }
    public boolean deleteAchievement(String userId, URL achievementURL) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = delete(buildURL(userId, "achievements"),
                            new HttpParameter[] {new HttpParameter("achievement", achievementURL.toString())});
        return Boolean.valueOf(res.asString().trim());
    }

    /* Activity Methods */
    
    public ResponseList<Activity> getActivities() throws FacebookException {
        return getActivities("me", null);
    }
    public ResponseList<Activity> getActivities(Reading reading) throws FacebookException {
        return getActivities("me", reading);
    }
    public ResponseList<Activity> getActivities(String userId) throws FacebookException {
        return getActivities(userId, null);
    }
    public ResponseList<Activity> getActivities(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createActivityList(get(buildURL(userId, "activities", reading)));
    }

    /* Album Methods */
    
    public ResponseList<Album> getAlbums() throws FacebookException {
        return getAlbums("me", null);
    }
    public ResponseList<Album> getAlbums(Reading reading) throws FacebookException {
        return getAlbums("me", reading);
    }
    public ResponseList<Album> getAlbums(String userId) throws FacebookException {
        return getAlbums(userId, null);
    }
    public ResponseList<Album> getAlbums(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createAlbumList(get(buildURL(userId, "albums", reading)));
    }

    public Album getAlbum(String albumId) throws FacebookException {
        return getAlbum(albumId, null);
    }
    public Album getAlbum(String albumId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createAlbum(get(buildURL(albumId, reading)));
    }
    
    public String createAlbum(AlbumCreate albumCreate) throws FacebookException {
        return createAlbum("me", albumCreate);
    }
    public String createAlbum(String userId, AlbumCreate albumCreate) throws FacebookException {
        ensureAuthorizationEnabled();
        JSONObject json = post(buildURL(userId, "albums"), albumCreate.asHttpParameterArray())
                          .asJSONObject();
        try {
            return json.getString("id");
        } catch (JSONException jsone) {
            throw new FacebookException(jsone.getMessage(), jsone);
        }
    }

    public ResponseList<Photo> getAlbumPhotos(String albumId) throws FacebookException {
        return getAlbumPhotos(albumId, null);
    }
    public ResponseList<Photo> getAlbumPhotos(String albumId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createPhotoList(get(buildURL(albumId, "photos", reading)));
    }

    public String addAlbumPhoto(String albumId, Media source) throws FacebookException {
        return addAlbumPhoto(albumId, source, null);
    }
    public String addAlbumPhoto(String albumId, Media source, String message) throws FacebookException {
        ensureAuthorizationEnabled();
        List<HttpParameter> httpParams = new ArrayList<HttpParameter>();
        httpParams.add(source.asHttpParameter("source"));
        if (message != null) {
            httpParams.add(new HttpParameter("message", message));
        }
        JSONObject json = post(buildURL(albumId, "photos"), 
                               httpParams.toArray(new HttpParameter[httpParams.size()]))
                          .asJSONObject();
        try {
            return json.getString("id");
        } catch (JSONException jsone) {
            throw new FacebookException(jsone.getMessage(), jsone);
        }
    }
    
    public ResponseList<Comment> getAlbumComments(String albumId) throws FacebookException {
        return getAlbumComments(albumId, null);
    }
    public ResponseList<Comment> getAlbumComments(String albumId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return _getComments(albumId, reading);
    }

    public String commentAlbum(String albumId, String message) throws FacebookException {
        ensureAuthorizationEnabled();
        return _comment(albumId, message);
    }

    public ResponseList<Like> getAlbumLikes(String albumId) throws FacebookException {
        return getAlbumLikes(albumId, null);
    }
    public ResponseList<Like> getAlbumLikes(String albumId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createLikeList(get(buildURL(albumId, "likes", reading)));
    }

    public boolean likeAlbum(String albumId) throws FacebookException {
        ensureAuthorizationEnabled();
        return _like(albumId);
    }
    public boolean unlikeAlbum(String albumId) throws FacebookException {
        ensureAuthorizationEnabled();
        return _unlike(albumId);
    }

    public URL getAlbumCoverPhoto(String albumId) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = get(buildURL(albumId, "picture"));
        try {
            return new URL(res.getResponseHeader("Location"));
        } catch (MalformedURLException urle) {
            throw new FacebookException(urle.getMessage(), urle);
        }
    }

    /* Book Methods */
    
    public ResponseList<Book> getBooks() throws FacebookException {
        return getBooks("me", null);
    }
    public ResponseList<Book> getBooks(Reading reading) throws FacebookException {
        return getBooks("me", reading);
    }
    public ResponseList<Book> getBooks(String userId) throws FacebookException {
        return getBooks(userId, null);
    }
    public ResponseList<Book> getBooks(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createBookList(get(buildURL(userId, "books", reading)));
    }

    /* Checkin Methods */
    
    public ResponseList<Checkin> getCheckins() throws FacebookException {
        return getCheckins("me", null);
    }
    public ResponseList<Checkin> getCheckins(Reading reading) throws FacebookException {
        return getCheckins("me", reading);
    }
    public ResponseList<Checkin> getCheckins(String userId) throws FacebookException {
        return getCheckins(userId, null);
    }
    public ResponseList<Checkin> getCheckins(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createCheckinList(get(buildURL(userId, "checkins", reading)));
    }

    public String checkin(CheckinCreate checkinCreate) throws FacebookException {
        return checkin("me", checkinCreate);
    }
    public String checkin(String userId, CheckinCreate checkinCreate) throws FacebookException {
        ensureAuthorizationEnabled();
        JSONObject json = post(buildURL(userId, "checkins"), checkinCreate.asHttpParameterArray())
                          .asJSONObject();
        return getRawString("id", json);
    }

    public Checkin getCheckin(String checkinId) throws FacebookException {
        return getCheckin(checkinId, null);
    }
    public Checkin getCheckin(String checkinId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createCheckin(get(buildURL(checkinId, reading)));
    }

    public ResponseList<Comment> getCheckinComments(String checkinId) throws FacebookException {
        return getCheckinComments(checkinId, null);
    }
    public ResponseList<Comment> getCheckinComments(String checkinId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return _getComments(checkinId, reading);
    }
    public String commentCheckin(String checkinId, String message) throws FacebookException {
        ensureAuthorizationEnabled();
        return _comment(checkinId, message);
    }

    public ResponseList<Like> getCheckinLikes(String checkinId) throws FacebookException {
        return getCheckinLikes(checkinId, null);
    }
    public ResponseList<Like> getCheckinLikes(String checkinId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createLikeList(get(buildURL(checkinId, "likes", reading)));
    }
    public boolean likeCheckin(String checkinId) throws FacebookException {
        ensureAuthorizationEnabled();
        return _like(checkinId);
    }
    public boolean unlikeCheckin(String checkinId) throws FacebookException {
        ensureAuthorizationEnabled();
        return _unlike(checkinId);
    }

    /* Domain Methods */
    
    public Domain getDomain(String domainId) throws FacebookException {
        return factory.createDomain(get(buildURL(domainId)));
    }
    public Domain getDomainByName(String domainName) throws FacebookException {
        return factory.createDomain(get(conf.getRestBaseURL(),
                new HttpParameter[]{new HttpParameter("domain", domainName)}));
    }
    public List<Domain> getDomainsByName(String... domainName) throws FacebookException {
        String domainNames = z_F4JInternalStringUtil.join(domainName);
        return factory.createDomainArray(get(conf.getRestBaseURL(),
                new HttpParameter[]{new HttpParameter("domains", domainNames)}));
    }

    /* Event Methods */
    
    public ResponseList<Event> getEvents() throws FacebookException {
        return getEvents("me", null);
    }
    public ResponseList<Event> getEvents(Reading reading) throws FacebookException {
        return getEvents("me", reading);
    }
    public ResponseList<Event> getEvents(String userId) throws FacebookException {
        return getEvents(userId, null);
    }
    public ResponseList<Event> getEvents(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createEventList(get(buildURL(userId, "events", reading)));
    }

    public String createEvent(EventUpdate eventUpdate) throws FacebookException {
        return createEvent("me", eventUpdate);
    }
    public String createEvent(String userId, EventUpdate eventUpdate) throws FacebookException {
        ensureAuthorizationEnabled();
        JSONObject json = post(buildURL(userId, "events"), eventUpdate.asHttpParameterArray())
                          .asJSONObject();
        return getRawString("id", json);
    }

    public Event getEvent(String eventId) throws FacebookException {
        return getEvent(eventId, null);
    }
    public Event getEvent(String eventId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = get(buildURL(eventId, reading));
        String resStr = res.asString().trim();
        if (resStr.equals("false")) {
            return null;
        }
        return factory.createEvent(res);
    }

    public boolean editEvent(String eventId, EventUpdate eventUpdate) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = post(buildURL(eventId), eventUpdate.asHttpParameterArray());
        return Boolean.valueOf(res.asString().trim());
    }

    public boolean deleteEvent(String eventId) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = delete(buildURL(eventId));
        return Boolean.valueOf(res.asString().trim());
    }

    public ResponseList<Post> getEventFeed(String eventId) throws FacebookException {
        return getEventFeed(eventId, null);
    }
    public ResponseList<Post> getEventFeed(String eventId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createPostList(get(buildURL(eventId, reading)));
    }

    public String postEventFeed(String eventId, PostUpdate postUpdate) throws FacebookException {
        ensureAuthorizationEnabled();
        return _postFeed(eventId, postUpdate);
    }

    public String postEventLink(String eventId, URL link) throws FacebookException {
        return postEventLink(eventId, link, null);
    }
    public String postEventLink(String eventId, URL link, String message) throws FacebookException {
        ensureAuthorizationEnabled();
        return _postLink(eventId, link, message);
    }

    public String postEventStatusMessage(String eventId, String message) throws FacebookException {
        ensureAuthorizationEnabled();
        return _postStatusMessage(eventId, message);
    }

    public ResponseList<RSVPStatus> getRSVPStatusAsNoreply(String eventId) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createRSVPStatusList(get(buildURL(eventId, "noreply")));
    }
    public ResponseList<RSVPStatus> getRSVPStatusAsNoreply(String eventId, String userId) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createRSVPStatusList(get(buildURL(eventId, "noreply/" + userId)));
    }

    public ResponseList<RSVPStatus> getRSVPStatusAsInvited(String eventId) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createRSVPStatusList(get(buildURL(eventId, "invited")));
    }
    public ResponseList<RSVPStatus> getRSVPStatusAsInvited(String eventId, String userId) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createRSVPStatusList(get(buildURL(eventId, "invited/" + userId)));
    }

    public boolean inviteToEvent(String eventId, String userId) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = post(buildURL(eventId, "invited/" + userId));
        return Boolean.valueOf(res.asString().trim());
    }
    public boolean inviteToEvent(String eventId, String[] userIds) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = post(buildURL(eventId, "invited"), new HttpParameter[] {
                                    new HttpParameter("users", z_F4JInternalStringUtil.join(userIds))});
        return Boolean.valueOf(res.asString().trim());
    }

    public boolean uninviteFromEvent(String eventId, String userId) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = delete(buildURL(eventId, "invited/" + userId));
        return Boolean.valueOf(res.asString().trim());
    }

    public ResponseList<RSVPStatus> getRSVPStatusInAttending(String eventId) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createRSVPStatusList(get(buildURL(eventId, "attnding")));
    }
    public ResponseList<RSVPStatus> getRSVPStatusInAttending(String eventId, String userId) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createRSVPStatusList(get(buildURL(eventId, "attending/" + userId)));
    }

    public boolean rsvpEventAsAttending(String eventId) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = post(buildURL(eventId, "attending"));
        return Boolean.valueOf(res.asString().trim());
    }

    public ResponseList<RSVPStatus> getRSVPStatusInMaybe(String eventId) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createRSVPStatusList(get(buildURL(eventId, "maybe")));
    }
    public ResponseList<RSVPStatus> getRSVPStatusInMaybe(String eventId, String userId) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createRSVPStatusList(get(buildURL(eventId, "maybe/" + userId)));
    }

    public boolean rsvpEventAsMaybe(String eventId) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = post(buildURL(eventId, "maybe"));
        return Boolean.valueOf(res.asString().trim());
    }

    public ResponseList<RSVPStatus> getRSVPStatusInDeclined(String eventId) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createRSVPStatusList(get(buildURL(eventId, "declined")));
    }
    public ResponseList<RSVPStatus> getRSVPStatusInDeclined(String eventId, String userId) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createRSVPStatusList(get(buildURL(eventId, "declined/" + userId)));
    }

    public boolean rsvpEventAsDeclined(String eventId) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = post(buildURL(eventId, "declined"));
        return Boolean.valueOf(res.asString().trim());
    }

    public URL getEventPictureURL(String eventId) throws FacebookException {
        return getEventPictureURL(eventId, null);
    }
    public URL getEventPictureURL(String eventId, PictureSize size) throws FacebookException {
        ensureAuthorizationEnabled();
        return _getPictureURL(eventId, "picture", size);
    }

    public boolean updateEventPicture(String eventId, Media source) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = post(buildURL(eventId, "picture"),
                                new HttpParameter[] {source.asHttpParameter("source")});
        return Boolean.valueOf(res.asString().trim());
    }

    public boolean deleteEventPicture(String eventId) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = delete(buildURL(eventId, "picture"));
        return Boolean.valueOf(res.asString().trim());
    }

    public ResponseList<Photo> getEventPhotos(String eventId) throws FacebookException {
        return getEventPhotos(eventId, null);
    }
    public ResponseList<Photo> getEventPhotos(String eventId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createPhotoList(get(buildURL(eventId, "photos", reading)));
    }

    public String postEventPhoto(String eventId, Media source) throws FacebookException {
        return postEventPhoto(eventId, source, null);
    }
    public String postEventPhoto(String eventId, Media source, String message) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpParameter[] httpParameters = new HttpParameter[] {source.asHttpParameter("source")};
        if (message != null) {
            httpParameters = HttpParameter.merge(httpParameters,
                                new HttpParameter[]{new HttpParameter("message", message)});
        }
        JSONObject json = post(buildURL(eventId, "photos"), httpParameters).asJSONObject();
        return getRawString("id", json);
    }

    public ResponseList<Video> getEventVideos(String eventId) throws FacebookException {
        return getEventVideos(eventId, null);
    }
    public ResponseList<Video> getEventVideos(String eventId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createVideoList(get(buildURL(eventId, "videos", reading)));
    }

    public String postEventVideo(String eventId, Media source) throws FacebookException {
        return postEventVideo(eventId, source, null, null);
    }
    public String postEventVideo(String eventId, Media source, String title, String description) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpParameter[] httpParameters = new HttpParameter[] {source.asHttpParameter("source")};
        if (title != null) {
            httpParameters = HttpParameter.merge(httpParameters,
                                new HttpParameter[]{new HttpParameter("title", title)});
        }
        if (description != null) {
            httpParameters = HttpParameter.merge(httpParameters,
                    new HttpParameter[]{new HttpParameter("description", description)});
        }
        JSONObject json = post(buildVideoURL(eventId, "videos"), httpParameters).asJSONObject();
        return getRawString("id", json);
    }

    /* Family Methods */
    
    public ResponseList<Family> getFamily() throws FacebookException {
        return getFamily("me", null);
    }
    public ResponseList<Family> getFamily(Reading reading) throws FacebookException {
        return getFamily("me", reading);
    }
    public ResponseList<Family> getFamily(String userId) throws FacebookException {
        return getFamily(userId, null);
    }
    public ResponseList<Family> getFamily(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createFamilyList(get(buildURL(userId, "family", reading)));
    }

    /* Post Methods */
    
    public ResponseList<Post> getFeed() throws FacebookException {
        return getFeed("me", null);
    }
    public ResponseList<Post> getFeed(Reading reading) throws FacebookException {
        return getFeed("me", reading);
    }
    public ResponseList<Post> getFeed(String userId) throws FacebookException {
        return getFeed(userId, null);
    }
    public ResponseList<Post> getFeed(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createPostList(get(buildURL(userId, "feed", reading)));
    }

    public ResponseList<Post> getHome() throws FacebookException {
        return getHome(null);
    }
    public ResponseList<Post> getHome(Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createPostList(get(buildURL("me", "home", reading)));
    }

    public ResponseList<Post> getPosts() throws FacebookException {
        return getPosts("me", null);
    }
    public ResponseList<Post> getPosts(Reading reading) throws FacebookException {
        return getPosts("me", reading);
    }
    public ResponseList<Post> getPosts(String userId) throws FacebookException {
        return getPosts(userId, null);
    }
    public ResponseList<Post> getPosts(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createPostList(get(buildURL(userId, "posts", reading)));
    }

    public ResponseList<Post> getStatuses() throws FacebookException {
        return getStatuses("me", null);
    }
    public ResponseList<Post> getStatuses(Reading reading) throws FacebookException {
        return getStatuses("me", reading);
    }
    public ResponseList<Post> getStatuses(String userId) throws FacebookException {
        return getStatuses(userId, null);
    }
    public ResponseList<Post> getStatuses(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createPostList(get(buildURL(userId, "statuses", reading)));
    }

    public Post getPost(String postId) throws FacebookException {
        return getPost(postId, null);
    }
    public Post getPost(String postId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createPost(get(buildURL(postId, reading)));
    }

    public boolean deletePost(String postId) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = delete(buildURL(postId));
        return Boolean.valueOf(res.asString().trim());
    }

    public ResponseList<Comment> getPostComments(String postId) throws FacebookException {
        return getPostComments(postId, null);
    }
    public ResponseList<Comment> getPostComments(String postId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return _getComments(postId, reading);
    }

    public String commentPost(String postId, String message) throws FacebookException {
        ensureAuthorizationEnabled();
        return _comment(postId, message);
    }
    
    public ResponseList<Like> getPostLikes(String postId) throws FacebookException {
        return getPostLikes(postId, null);
    }
    public ResponseList<Like> getPostLikes(String postId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createLikeList(get(buildURL(postId, "likes", reading)));
    }

    public boolean likePost(String postId) throws FacebookException {
        ensureAuthorizationEnabled();
        return _like(postId);
    }
    public boolean unlikePost(String postId) throws FacebookException {
        ensureAuthorizationEnabled();
        return _unlike(postId);
    }

    public ResponseList<Insight> getPostInsights(String postId) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createInsightList(get(buildURL(postId, "insights")));
    }
    
    public String postFeed(PostUpdate postUpdate) throws FacebookException {
        return postFeed("me", postUpdate);
    }
    public String postFeed(String userId, PostUpdate postUpdate) throws FacebookException {
        ensureAuthorizationEnabled();
        return _postFeed(userId, postUpdate);
    }

    public String postStatusMessage(String message) throws FacebookException {
        return postStatusMessage("me", message);
    }
    public String postStatusMessage(String userId, String message) throws FacebookException {
        ensureAuthorizationEnabled();
        return _postStatusMessage(userId, message);
    }

    /* Friend Methods */
    
    public ResponseList<Post> getTagged() throws FacebookException {
        return getTagged("me", null);
    }
    public ResponseList<Post> getTagged(Reading reading) throws FacebookException {
        return getTagged("me", reading);
    }
    public ResponseList<Post> getTagged(String userId) throws FacebookException {
        return getTagged(userId, null);
    }
    public ResponseList<Post> getTagged(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createPostList(get(buildURL(userId, "tagged", reading)));
    }

    public ResponseList<Friendlist> getFriendlists() throws FacebookException {
        return getFriendlists("me", null);
    }
    public ResponseList<Friendlist> getFriendlists(Reading reading) throws FacebookException {
        return getFriendlists("me", reading);
    }
    public ResponseList<Friendlist> getFriendlists(String userId) throws FacebookException {
        return getFriendlists(userId, null);
    }
    public ResponseList<Friendlist> getFriendlists(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createFriendlistList(get(buildURL(userId, "friendlists", reading)));
    }

    public ResponseList<FriendRequest> getFriendRequests() throws FacebookException {
        return getFriendRequests("me", null);
    }
    public ResponseList<FriendRequest> getFriendRequests(Reading reading) throws FacebookException {
        return getFriendRequests("me", reading);
    }
    public ResponseList<FriendRequest> getFriendRequests(String userId) throws FacebookException {
        return getFriendRequests(userId, null);
    }
    public ResponseList<FriendRequest> getFriendRequests(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createFriendRequestList(get(buildURL(userId, "friendrequests", reading)));
    }

    public ResponseList<Friend> getFriends() throws FacebookException {
        return getFriends("me", null);
    }
    public ResponseList<Friend> getFriends(Reading reading) throws FacebookException {
        return getFriends("me", reading);
    }
    public ResponseList<Friend> getFriends(String userId) throws FacebookException {
        return getFriends(userId, null);
    }
    public ResponseList<Friend> getFriends(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createFriendList(get(buildURL(userId, "friends", reading)));
    }

    public ResponseList<Friend> getMutualFriends(String friendUserId) throws FacebookException {
        return getMutualFriends("me", friendUserId, null);
    }
    public ResponseList<Friend> getMutualFriends(String friendUserId, Reading reading) throws FacebookException {
        return getMutualFriends("me", friendUserId, reading);
    }
    public ResponseList<Friend> getMutualFriends(String userId1, String userId2) throws FacebookException {
        return getMutualFriends(userId1, userId2, null);
    }
    public ResponseList<Friend> getMutualFriends(String userId1, String userId2, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createFriendList(get(buildURL(userId1, "mutualfriends/" + userId2, reading)));
    }

    public String createFriendlist(String friendlistName) throws FacebookException {
        return createFriendlist("me", friendlistName);
    }
    public String createFriendlist(String userId, String friendlistName) throws FacebookException {
        ensureAuthorizationEnabled();
        JSONObject json = post(buildURL(userId, "friendlists"),
                                new HttpParameter[]{
                                    new HttpParameter("name", friendlistName)
                                })
                          .asJSONObject();
        return getRawString("id", json);
    }
    public boolean deleteFriendlist(String friendlistId) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = delete(buildURL(friendlistId));
        return Boolean.valueOf(res.asString().trim());
    }

    public boolean addFriendlistMember(String friendlistId, String userId) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = post(buildURL(friendlistId + "/members/" + userId));
        return Boolean.valueOf(res.asString().trim());
    }

    public boolean removeFriendlistMember(String friendlistId, String userId) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = delete(buildURL(friendlistId + "/members/" + userId));
        return Boolean.valueOf(res.asString().trim());
    }

    public Friendlist getFriendlist(String friendlistId) throws FacebookException {
        return getFriendlist(friendlistId, null);
    }
    public Friendlist getFriendlist(String friendlistId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createFriendlist(get(buildURL(friendlistId, reading)));
    }

    public ResponseList<Friend> getFriendlistMembers(String friendlistId) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createFriendList(get(buildURL(friendlistId)));
    }

    public ResponseList<Friend> getBelongsFriend(String friendId) throws FacebookException {
        return getBelongsFriend("me", friendId);
    }
    public ResponseList<Friend> getBelongsFriend(String friendId, Reading reading) throws FacebookException {
        return getBelongsFriend("me", friendId, reading);
    }
    public ResponseList<Friend> getBelongsFriend(String userId, String friendId) throws FacebookException {
        return getBelongsFriend(userId, friendId, null);
    }
    public ResponseList<Friend> getBelongsFriend(String userId, String friendId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createFriendList(get(buildURL(userId, "friends/" + friendId, reading)));
    }

    /* Favorite Methods */
    
    public ResponseList<Game> getGames() throws FacebookException {
        return getGames("me", null);
    }
    public ResponseList<Game> getGames(Reading reading) throws FacebookException {
        return getGames("me", reading);
    }
    public ResponseList<Game> getGames(String userId) throws FacebookException {
        return getGames(userId, null);
    }
    public ResponseList<Game> getGames(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createGameList(get(buildURL(userId, "games", reading)));
    }

    public ResponseList<Movie> getMovies() throws FacebookException {
        return getMovies("me", null);
    }
    public ResponseList<Movie> getMovies(Reading reading) throws FacebookException {
        return getMovies("me", reading);
    }
    public ResponseList<Movie> getMovies(String userId) throws FacebookException {
        return getMovies(userId, null);
    }
    public ResponseList<Movie> getMovies(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createMovieList(get(buildURL(userId, "movies", reading)));
    }

    public ResponseList<Music> getMusic() throws FacebookException {
        return getMusic("me", null);
    }
    public ResponseList<Music> getMusic(Reading reading) throws FacebookException {
        return getMusic("me", reading);
    }
    public ResponseList<Music> getMusic(String userId) throws FacebookException {
        return getMusic(userId, null);
    }
    public ResponseList<Music> getMusic(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createMusicList(get(buildURL(userId, "music", reading)));
    }

    public ResponseList<Television> getTelevision() throws FacebookException {
        return getTelevision("me", null);
    }
    public ResponseList<Television> getTelevision(Reading reading) throws FacebookException {
        return getTelevision("me", reading);
    }
    public ResponseList<Television> getTelevision(String userId) throws FacebookException {
        return getTelevision(userId, null);
    }
    public ResponseList<Television> getTelevision(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createTelevisionList(get(buildURL(userId, "television", reading)));
    }

    public ResponseList<Interest> getInterests() throws FacebookException {
        return getInterests("me", null);
    }
    public ResponseList<Interest> getInterests(Reading reading) throws FacebookException {
        return getInterests("me", reading);
    }
    public ResponseList<Interest> getInterests(String userId) throws FacebookException {
        return getInterests(userId, null);
    }
    public ResponseList<Interest> getInterests(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createInterestList(get(buildURL(userId, "interests", reading)));
    }

    /* Group Methods */
    
    public ResponseList<Group> getGroups() throws FacebookException {
        return getGroups("me", null);
    }
    public ResponseList<Group> getGroups(Reading reading) throws FacebookException {
        return getGroups("me", reading);
    }
    public ResponseList<Group> getGroups(String userId) throws FacebookException {
        return getGroups(userId, null);
    }
    public ResponseList<Group> getGroups(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createGroupList(get(buildURL(userId, "groups", reading)));
    }

    public Group getGroup(String groupId) throws FacebookException {
        return getGroup(groupId, null);
    }
    public Group getGroup(String groupId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createGroup(get(buildURL(groupId, reading)));
    }

    public ResponseList<Post> getGroupFeed(String groupId) throws FacebookException {
        return getGroupFeed(groupId, null);
    }
    public ResponseList<Post> getGroupFeed(String groupId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createPostList(get(buildURL(groupId, "feed", reading)));
    }

    public ResponseList<GroupMember> getGroupMembers(String groupId) throws FacebookException {
        return getGroupMembers(groupId, null);
    }
    public ResponseList<GroupMember> getGroupMembers(String groupId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createGroupMemberList(get(buildURL(groupId, "members", reading)));
    }

    public URL getGroupPictureURL(String groupId) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = get(buildURL(groupId, "picture"));
        try {
            return new URL(res.getResponseHeader("Location"));
        } catch (MalformedURLException urle) {
            throw new FacebookException(urle.getMessage(), urle);
        }
    }

    public ResponseList<GroupDoc> getGroupDocs(String groupId) throws FacebookException {
        return getGroupDocs(groupId, null);
    }
    public ResponseList<GroupDoc> getGroupDocs(String groupId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createGroupDocList(get(buildURL(groupId, "docs", reading)));
    }

    public String postGroupLink(String groupId, URL linkURL) throws FacebookException {
        return postGroupLink(groupId, linkURL, null);
    }
    public String postGroupLink(String groupId, URL linkURL, String message) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpParameter[] httpParameters = new HttpParameter[]{new HttpParameter("link", linkURL.toString())};
        if (message != null) {
            httpParameters = HttpParameter.merge(httpParameters,
                                new HttpParameter[] {new HttpParameter("message", message)});
        }
        JSONObject json = post(buildURL(groupId, "feed"), httpParameters).asJSONObject();
        return getRawString("id", json);
    }
    public String postGroupFeed(String groupId, PostUpdate postUpdate) throws FacebookException {
        ensureAuthorizationEnabled();
        JSONObject json = post(buildURL(groupId, "feed"),
                            postUpdate.asHttpParameterArray()
                          ).asJSONObject();
        return getRawString("id", json);
    }
    public String postGroupStatusMessage(String groupId, String message) throws FacebookException {
        ensureAuthorizationEnabled();
        JSONObject json = post(buildURL(groupId, "feed"), new HttpParameter[]{
                            new HttpParameter("message", message)
                          }).asJSONObject();
        return getRawString("id", json);
    }

    /* Message Methods */
    
    public InboxResponseList<Inbox> getInbox() throws FacebookException {
        return getInbox("me", null);
    }
    public InboxResponseList<Inbox> getInbox(Reading reading) throws FacebookException {
        return getInbox("me", reading);
    }
    public InboxResponseList<Inbox> getInbox(String userId) throws FacebookException {
        return getInbox(userId, null);
    }
    public InboxResponseList<Inbox> getInbox(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createInboxList(get(buildURL(userId, "inbox", reading)));
    }

    public ResponseList<Message> getOutbox() throws FacebookException {
        return getOutbox("me", null);
    }
    public ResponseList<Message> getOutbox(Reading reading) throws FacebookException {
        return getOutbox("me", reading);
    }
    public ResponseList<Message> getOutbox(String userId) throws FacebookException {
        return getOutbox(userId, null);
    }
    public ResponseList<Message> getOutbox(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createMessageList(get(buildURL(userId, "outbox", reading)));
    }

    public ResponseList<Message> getUpdates() throws FacebookException {
        return getUpdates("me", null);
    }
    public ResponseList<Message> getUpdates(Reading reading) throws FacebookException {
        return getUpdates("me", reading);
    }
    public ResponseList<Message> getUpdates(String userId) throws FacebookException {
        return getUpdates(userId, null);
    }
    public ResponseList<Message> getUpdates(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createMessageList(get(buildURL(userId, "updates", reading)));
    }

    public Message getMessage(String messageId) throws FacebookException {
        return getMessage(messageId, null);
    }
    public Message getMessage(String messageId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createMessage(get(buildURL(messageId, reading)));
    }

    /* Like Methods */
    
    public ResponseList<Like> getUserLikes() throws FacebookException {
        return _getLikes("me", null);
    }
    public ResponseList<Like> getUserLikes(Reading reading) throws FacebookException {
        return _getLikes("me", reading);
    }
    public ResponseList<Like> getUserLikes(String userId) throws FacebookException {
        return _getLikes(userId, null);
    }
    public ResponseList<Like> getUserLikes(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createLikeList(get(buildURL(userId, "likes", reading)));
    }

    public ResponseList<Like> getLikesBelongs(String pageId) throws FacebookException {
        return getLikesBelongs("me", pageId, null);
    }
    public ResponseList<Like> getLikesBelongs(String pageId, Reading reading) throws FacebookException {
        return getLikesBelongs("me", pageId, reading);
    }
    public ResponseList<Like> getLikesBelongs(String userId, String pageId) throws FacebookException {
        return getLikesBelongs(userId, pageId, null);
    }
    public ResponseList<Like> getLikesBelongs(String userId, String pageId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createLikeList(get(buildURL(userId, "likes/" + pageId, reading)));
    }

    /* Comment Methods */

    public Comment getComment(String commentId) throws FacebookException {
        return factory.createComment(get(buildURL(commentId)));
    }
    public boolean deleteComment(String commentId) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = delete(buildURL(commentId));
        return Boolean.valueOf(res.asString().trim());
    }

    public ResponseList<Like> getCommentLikes(String commentId) throws FacebookException {
        return getCommentLikes(commentId, null);
    }
    public ResponseList<Like> getCommentLikes(String commentId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createLikeList(get(buildURL(commentId, "likes", reading)));
    }
    public boolean likeComment(String commentId) throws FacebookException {
        ensureAuthorizationEnabled();
        return _like(commentId);
    }
    public boolean unlikeComment(String commentId) throws FacebookException {
        ensureAuthorizationEnabled();
        return _unlike(commentId);
    }

    /* Link Methods */
    
    public ResponseList<Link> getLinks() throws FacebookException {
        return getLinks("me", null);
    }
    public ResponseList<Link> getLinks(Reading reading) throws FacebookException {
        return getLinks("me", reading);
    }
    public ResponseList<Link> getLinks(String userId) throws FacebookException {
        return getLinks(userId, null);
    }
    public ResponseList<Link> getLinks(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createLinkList(get(buildURL(userId, "links", reading)));
    }

    public Link getLink(String linkId) throws FacebookException {
        return getLink(linkId, null);
    }
    public Link getLink(String linkId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createLink(get(buildURL(linkId, reading)));
    }

    public ResponseList<Comment> getLinkComments(String linkId) throws FacebookException {
        return getLinkComments(linkId, null);
    }
    public ResponseList<Comment> getLinkComments(String linkId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return _getComments(linkId, reading);
    }

    public String commentLink(String linkId, String message) throws FacebookException {
        ensureAuthorizationEnabled();
        return _comment(linkId, message);
    }

    public ResponseList<Like> getLinkLikes(String linkId) throws FacebookException {
        return getLinkLikes(linkId, null);
    }
    public ResponseList<Like> getLinkLikes(String linkId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return _getLikes(linkId, reading);
    }

    public boolean likeLink(String linkId) throws FacebookException {
        ensureAuthorizationEnabled();
        return _like(linkId);
    }
    public boolean unlikeLink(String linkId) throws FacebookException {
        ensureAuthorizationEnabled();
        return _unlike(linkId);
    }

    public String postLink(URL link) throws FacebookException {
        ensureAuthorizationEnabled();
        return _postLink("me", link, null);
    }
    public String postLink(URL link, String message) throws FacebookException {
        ensureAuthorizationEnabled();
        return _postLink("me", link, message);
    }
    public String postLink(String userId, URL link) throws FacebookException {
        ensureAuthorizationEnabled();
        return _postLink(userId, link, null);
    }
    public String postLink(String userId, URL link, String message) throws FacebookException {
        ensureAuthorizationEnabled();
        return _postLink(userId, link, message);
    }

    /* Location Methods */
    
    public ResponseList<Location> getLocations() throws FacebookException {
        return getLocations("me", null);
    }
    public ResponseList<Location> getLocations(Reading reading) throws FacebookException {
        return getLocations("me", reading);
    }
    public ResponseList<Location> getLocations(String userId) throws FacebookException {
        return getLocations(userId, null);
    }
    public ResponseList<Location> getLocations(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createLocationList(get(buildURL(userId, "locations", reading)));
    }

    /* Note Methods */
    
    public ResponseList<Note> getNotes() throws FacebookException {
        return getNotes("me", null);
    }
    public ResponseList<Note> getNotes(Reading reading) throws FacebookException {
        return getNotes("me", reading);
    }
    public ResponseList<Note> getNotes(String userId) throws FacebookException {
        return getNotes(userId, null);
    }
    public ResponseList<Note> getNotes(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createNoteList(get(buildURL(userId, "notes", reading)));
    }

    public String createNote(String subject, String message) throws FacebookException {
        return createNote("me", subject, message);
    }
    public String createNote(String userId, String subject, String message) throws FacebookException {
        ensureAuthorizationEnabled();
        JSONObject json = post(buildURL(userId, "notes"), new HttpParameter[] {
                            new HttpParameter("subject", subject),
                            new HttpParameter("message", message)
                          }).asJSONObject();
        try {
            return json.getString("id");
        } catch (JSONException jsone) {
            throw new FacebookException(jsone.getMessage(), jsone);
        }
    }
    
    public Note getNote(String noteId) throws FacebookException {
        return getNote(noteId, null);
    }
    public Note getNote(String noteId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createNote(get(buildURL(noteId, reading)));
    }

    public ResponseList<Comment> getNoteComments(String noteId) throws FacebookException {
        return getNoteComments(noteId, null);
    }
    public ResponseList<Comment> getNoteComments(String noteId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return _getComments(noteId, reading);
    }

    public String commentNote(String noteId, String message) throws FacebookException {
        ensureAuthorizationEnabled();
        return _comment(noteId, message);
    }

    public ResponseList<Like> getNoteLikes(String noteId) throws FacebookException {
        return getNoteLikes(noteId, null);
    }
    public ResponseList<Like> getNoteLikes(String noteId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return _getLikes(noteId, reading);
    }

    public boolean likeNote(String noteId) throws FacebookException {
        ensureAuthorizationEnabled();
        return _like(noteId);
    }
    public boolean unlikeNote(String noteId) throws FacebookException {
        ensureAuthorizationEnabled();
        return _unlike(noteId);
    }

    /* Notification Methods */
    
    public ResponseList<Notification> getNotifications() throws FacebookException {
        return getNotifications("me", null);
    }
    public ResponseList<Notification> getNotifications(boolean includeRead) throws FacebookException {
        return getNotifications("me", null, includeRead);
    }
    public ResponseList<Notification> getNotifications(Reading reading) throws FacebookException {
        return getNotifications("me", reading);
    }
    public ResponseList<Notification> getNotifications(Reading reading, boolean includeRead) throws FacebookException {
        return getNotifications("me", reading, includeRead);
    }
    public ResponseList<Notification> getNotifications(String userId) throws FacebookException {
        return getNotifications(userId, null);
    }
    public ResponseList<Notification> getNotifications(String userId, boolean includeRead) throws FacebookException {
        return getNotifications(userId, null, includeRead);
    }
    public ResponseList<Notification> getNotifications(String userId, Reading reading) throws FacebookException {
        return getNotifications(userId, reading, false);
    }
    public ResponseList<Notification> getNotifications(String userId, Reading reading, boolean includeRead) throws FacebookException {
        ensureAuthorizationEnabled();
        String url = buildURL(userId, "notifications", reading);
        HttpResponse res;
        if (includeRead) {
            res = get(url, new HttpParameter[]{new HttpParameter("include_read", 1)});
        } else {
            res = get(url);
        }
        return factory.createNotificationList(res);
    }

    public boolean markNotificationAsRead(String notificationId) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = post(buildURL(notificationId), new HttpParameter[] {new HttpParameter("unread", 0)});
        return Boolean.valueOf(res.asString().trim());
    }

    /* Page Methods */

    public Page getPage(String pageId) throws FacebookException {
        return getPage(pageId, null);
    }
    public Page getPage(String pageId, Reading reading) throws FacebookException {
        HttpResponse res = get(buildURL(pageId, reading));
        return factory.createPage(res);
    }

    public Page getLikedPage(String pageId) throws FacebookException {
        return getLikedPage("me", pageId, null);
    }
    public Page getLikedPage(String pageId, Reading reading) throws FacebookException {
        return getLikedPage("me", pageId, reading);
    }
    public Page getLikedPage(String userId, String pageId) throws FacebookException {
        return getLikedPage(userId, pageId, null);
    }
    public Page getLikedPage(String userId, String pageId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = get(buildURL(userId, "likes/" + pageId, reading));
        ResponseList<Page> list = factory.createPageList(res);
        return list.size() == 0 ? null : list.get(0);
    }

    /* Permission Methods */

    public List<Permission> getPermissions() throws FacebookException {
        return getPermissions("me");
    }
    public List<Permission> getPermissions(String userId) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createPermissions(get(buildURL(userId, "permissions")));
    }

    public boolean revokePermission(String permissionName) throws FacebookException {
        return revokePermission("me", permissionName);
    }
    public boolean revokePermission(String userId, String permissionName) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = delete(buildURL(userId, "permissions/" + permissionName));
        return Boolean.valueOf(res.asString().trim());
    }

    /* Photo Methods */
    
    public ResponseList<Photo> getPhotos() throws FacebookException {
        return getPhotos("me", null);
    }
    public ResponseList<Photo> getPhotos(Reading reading) throws FacebookException {
        return getPhotos("me", reading);
    }
    public ResponseList<Photo> getPhotos(String userId) throws FacebookException {
        return getPhotos(userId, null);
    }
    public ResponseList<Photo> getPhotos(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createPhotoList(get(buildURL(userId, "photos", reading)));
    }

    public Photo getPhoto(String photoId) throws FacebookException {
        return getPhoto(photoId, null);
    }
    public Photo getPhoto(String photoId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createPhoto(get(buildURL(photoId, reading)));
    }

    public ResponseList<Comment> getPhotoComments(String photoId) throws FacebookException {
        return getPhotoComments(photoId, null);
    }
    public ResponseList<Comment> getPhotoComments(String photoId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return _getComments(photoId, reading);
    }

    public String commentPhoto(String photoId, String message) throws FacebookException {
        ensureAuthorizationEnabled();
        return _comment(photoId, message);
    }

    public ResponseList<Like> getPhotoLikes(String photoId) throws FacebookException {
        return getPhotoLikes(photoId, null);
    }
    public ResponseList<Like> getPhotoLikes(String photoId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return _getLikes(photoId, reading);
    }

    public boolean likePhoto(String photoId) throws FacebookException {
        ensureAuthorizationEnabled();
        return _like(photoId);
    }
    public boolean unlikePhoto(String photoId) throws FacebookException {
        ensureAuthorizationEnabled();
        return _unlike(photoId);
    }

    public URL getPhotoURL(String photoId) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = get(buildURL(photoId, "picture"));
        try {
            return new URL(res.getResponseHeader("Location"));
        } catch (MalformedURLException urle) {
            throw new FacebookException(urle.getMessage(), urle);
        }
    }

    public ResponseList<Tag> getTagsOnPhoto(String photoId) throws FacebookException {
        return getTagsOnPhoto(photoId, null);
    }
    public ResponseList<Tag> getTagsOnPhoto(String photoId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createTagList(get(buildURL(photoId, reading)));
    }

    public boolean addTagToPhoto(String photoId, String toUserId) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = post(buildURL(photoId, "tags"), new HttpParameter[]{new HttpParameter("to", toUserId)});
        return Boolean.valueOf(res.asString().trim());
    }
    
    public boolean addTagToPhoto(String photoId, TagUpdate tagUpdate) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = post(buildURL(photoId, "tags"), tagUpdate.asHttpParameterArray());
        return Boolean.valueOf(res.asString().trim());
   }

    public boolean addTagToPhoto(String photoId, List<String> toUserIds) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = post(buildURL(photoId, "tags"), new HttpParameter[]{new HttpParameter("tags", new JSONArray(toUserIds).toString())});
        return Boolean.valueOf(res.asString().trim());
    }

    public boolean updateTagOnPhoto(String photoId, String toUserId) throws FacebookException {
        return addTagToPhoto(photoId, toUserId);
    }
    
    public boolean updateTagOnPhoto(String photoId, TagUpdate tagUpdate) throws FacebookException {
        return addTagToPhoto(photoId, tagUpdate);
    }

    public String postPhoto(Media source) throws FacebookException {
        return postPhoto("me", source);
    }
    public String postPhoto(Media source, String message, String place, boolean noStory) throws FacebookException {
        return postPhoto("me", source, message, place, noStory);
    }
    public String postPhoto(String userId, Media source) throws FacebookException {
        return postPhoto(userId, source, null, null, false);
    }
    public String postPhoto(String userId, Media source, String message, String place, boolean noStory) throws FacebookException {
        ensureAuthorizationEnabled();
        List<HttpParameter> params = new ArrayList<HttpParameter>();
        params.add(source.asHttpParameter("source"));
        if (message != null) {
            params.add(new HttpParameter("message", message));
        }
        if (place != null) {
            params.add(new HttpParameter("place", place));
        }
        if (noStory) {
            params.add(new HttpParameter("no_story", 1));
        }
        HttpParameter[] httpParameters = (HttpParameter[]) params.toArray(new HttpParameter[params.size()]);

        JSONObject json = post(buildURL(userId, "photos"), httpParameters).asJSONObject();
        return getRawString("id", json);
    }

    public boolean deletePhoto(String photoId) throws FacebookException {
        HttpResponse res = delete(buildURL(photoId));
        return Boolean.valueOf(res.asString().trim());
    }

    /* Poke Methods */
    
    public ResponseList<Poke> getPokes() throws FacebookException {
        return getPokes("me", null);
    }
    public ResponseList<Poke> getPokes(Reading reading) throws FacebookException {
        return getPokes("me", reading);
    }
    public ResponseList<Poke> getPokes(String userId) throws FacebookException {
        return getPokes(userId, null);
    }
    public ResponseList<Poke> getPokes(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createPokeList(get(buildURL(userId, "pokes", reading)));
    }

    /* Question Methods */
    
    public ResponseList<Question> getQuestions() throws FacebookException {
        return getQuestions("me", null);
    }
    public ResponseList<Question> getQuestions(Reading reading) throws FacebookException {
        return getQuestions("me", reading);
    }
    public ResponseList<Question> getQuestions(String userId) throws FacebookException {
        return getQuestions(userId, null);
    }
    public ResponseList<Question> getQuestions(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createQuestionList(get(buildURL(userId, "questions", reading)));
    }

    public Question getQuestion(String questionId) throws FacebookException {
        return getQuestion(questionId, null);
    }
    public Question getQuestion(String questionId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createQuestion(get(buildURL(questionId, reading)));
    }

    public String createQuestion(String question) throws FacebookException {
        return createQuestion(question, null, false);
    }
    public String createQuestion(String userId, String question) throws FacebookException {
        return createQuestion(userId, question, null, false);
    }
    public String createQuestion(String question, List<String> options, boolean allowNewOptions) throws FacebookException {
        return createQuestion("me", question, options, allowNewOptions);
    }
    public String createQuestion(String userId, String question, List<String> options, boolean allowNewOptions) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpParameter[] httpParameters = new HttpParameter[]{
                                            new HttpParameter("question", question),
                                            new HttpParameter("allow_new_options", allowNewOptions)
                                         };
        if (options != null && options.size() != 0) {
            httpParameters = HttpParameter.merge(httpParameters, new HttpParameter[]{
                    new HttpParameter("options", new JSONArray(options).toString())
            });
        }
        JSONObject json = post(buildURL(userId, "questions"), httpParameters).asJSONObject();
        try {
            return json.getString("id");
        } catch (JSONException jsone) {
            throw new FacebookException(jsone.getMessage(), jsone);
        }
    }

    public boolean deleteQuestion(String questionId) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = delete(buildURL(questionId));
        return Boolean.valueOf(res.asString().trim());
   }

    public ResponseList<Option> getQuestionOptions(String questionId) throws FacebookException {
        return getQuestionOptions(questionId, null);
    }
    public ResponseList<Option> getQuestionOptions(String questionId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createQuestionOptionList(get(buildURL(questionId, "options", reading)));
    }

    public String addQuestionOption(String questionId, String optionDescription) throws FacebookException {
        ensureAuthorizationEnabled();
        JSONObject json = post(buildURL(questionId, "options"),
                                new HttpParameter[]{new HttpParameter("option", optionDescription)})
                          .asJSONObject();
        try {
            return json.getString("id");
        } catch (JSONException jsone) {
            throw new FacebookException(jsone.getMessage(), jsone);
        }
   }

    public ResponseList<QuestionVotes> getQuestionOptionVotes(String questionId) throws FacebookException {
        ensureAuthorizationEnabled();
        Reading reading = new Reading().fields("votes");
        return factory.createQuestionVotesList(get(buildURL(questionId, "options", reading)));
    }

    /* Game Methods */
    
    public ResponseList<Score> getScores() throws FacebookException {
        return getScores("me", null);
    }
    public ResponseList<Score> getScores(Reading reading) throws FacebookException {
        return getScores("me", reading);
    }
    public ResponseList<Score> getScores(String userId) throws FacebookException {
        return getScores(userId, null);
    }
    public ResponseList<Score> getScores(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createScoreList(get(buildURL(userId, "scores", reading)));
    }

    public boolean postScore(int scoreValue) throws FacebookException {
        return postScore("me", scoreValue);
    }
    public boolean postScore(String userId, int scoreValue) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = post(buildURL(userId, "scores"),
                            new HttpParameter[] {new HttpParameter("score", scoreValue)});
        return Boolean.valueOf(res.asString().trim());
    }

    public boolean deleteScore() throws FacebookException {
        return deleteScore("me");
    }
    public boolean deleteScore(String userId) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = delete(buildURL(userId, "scores"));
        return Boolean.valueOf(res.asString().trim());
    }

    /* Subscribe Methods */
    
    public ResponseList<Subscribedto> getSubscribedto() throws FacebookException {
        return getSubscribedto("me", null);
    }
    public ResponseList<Subscribedto> getSubscribedto(Reading reading) throws FacebookException {
        return getSubscribedto("me", reading);
    }
    public ResponseList<Subscribedto> getSubscribedto(String userId) throws FacebookException {
        return getSubscribedto(userId, null);
    }
    public ResponseList<Subscribedto> getSubscribedto(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createSubscribedtoList(get(buildURL(userId, "subscribedto", reading)));
    }

    public ResponseList<Subscriber> getSubscribers() throws FacebookException {
        return getSubscribers("me", null);
    }
    public ResponseList<Subscriber> getSubscribers(Reading reading) throws FacebookException {
        return getSubscribers("me", reading);
    }
    public ResponseList<Subscriber> getSubscribers(String userId) throws FacebookException {
        return getSubscribers(userId, null);
    }
    public ResponseList<Subscriber> getSubscribers(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createSubscriberList(get(buildURL(userId, "subscribers", reading)));
    }

    /* Video Methods */
    
    public ResponseList<Video> getVideos() throws FacebookException {
        return getVideos("me", null);
    }
    public ResponseList<Video> getVideos(Reading reading) throws FacebookException {
        return getVideos("me", reading);
    }
    public ResponseList<Video> getVideos(String userId) throws FacebookException {
        return getVideos(userId, null);
    }
    public ResponseList<Video> getVideos(String userId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createVideoList(get(buildURL(userId, "videos", reading)));
    }

    public String postVideo(Media source) throws FacebookException {
        return postVideo("me", source);
    }
    public String postVideo(Media source, String title, String description) throws FacebookException {
        return postVideo("me", source, title, description);
    }
    public String postVideo(String userId, Media source) throws FacebookException {
        return postVideo(userId, source, null, null);
    }
    public String postVideo(String userId, Media source, String title, String description) throws FacebookException {
        ensureAuthorizationEnabled();
        List<HttpParameter> params = new ArrayList<HttpParameter>();
        params.add(source.asHttpParameter("source"));
        if (title != null) {
            params.add(new HttpParameter("title", title));
        }
        if (description != null) {
            params.add(new HttpParameter("description", description));
        }
        HttpParameter[] httpParameters = (HttpParameter[]) params.toArray(new HttpParameter[params.size()]);

        JSONObject json = post(buildVideoURL(userId, "videos"), httpParameters).asJSONObject();
        return getRawString("id", json);
    }

    public Video getVideo(String videoId) throws FacebookException {
        return getVideo(videoId, null);
    }
    public Video getVideo(String videoId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createVideo(get(buildURL(videoId, reading)));
    }

    public ResponseList<Like> getVideoLikes(String videoId) throws FacebookException {
        return getVideoLikes(videoId, null);
    }
    public ResponseList<Like> getVideoLikes(String videoId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return _getLikes(videoId, reading);
    }

    public boolean likeVideo(String videoId) throws FacebookException {
        ensureAuthorizationEnabled();
        return _like(videoId);
    }
    public boolean unlikeVideo(String videoId) throws FacebookException {
        ensureAuthorizationEnabled();
        return _unlike(videoId);
    }

    public ResponseList<Comment> getVideoComments(String videoId) throws FacebookException {
        return getVideoComments(videoId, null);
    }
    public ResponseList<Comment> getVideoComments(String videoId, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return _getComments(videoId, reading);
    }

    public String commentVideo(String videoId, String message) throws FacebookException {
        ensureAuthorizationEnabled();
        return _comment(videoId, message);
    }

    public URL getVideoCover(String videoId) throws FacebookException {
        ensureAuthorizationEnabled();
        return _getPictureURL(videoId, "picture", null);
    }
    
    /* Insight Methods */
    
    public ResponseList<Insight> getInsights(String objectId, String metric) throws FacebookException {
        return getInsights(objectId, metric, null);
    }

    public ResponseList<Insight> getInsights(String objectId, String metric, Reading reading) throws FacebookException {
        ensureAuthorizationEnabled();
        return factory.createInsightList(get(buildURL(objectId, "insights/" + metric, reading)));
    }

    /* Search Methods */
    
    public ResponseList<Post> searchPosts(String query) throws FacebookException {
        return searchPosts(query, null);
    }

    public ResponseList<Post> searchPosts(String query, Reading reading) throws FacebookException {
        return factory.createPostList(get(buildSearchURL(query, "post", reading)));
    }

    public ResponseList<User> searchUsers(String query) throws FacebookException {
        return searchUsers(query, null);
    }

    public ResponseList<User> searchUsers(String query, Reading reading) throws FacebookException {
        return factory.createUserList(get(buildSearchURL(query, "user", reading)));
    }

    public ResponseList<Event> searchEvents(String query) throws FacebookException {
        return searchEvents(query, null);
    }

    public ResponseList<Event> searchEvents(String query, Reading reading) throws FacebookException {
        return factory.createEventList(get(buildSearchURL(query, "event", reading)));
    }

    public ResponseList<Group> searchGroups(String query) throws FacebookException {
        return searchGroups(query, null);
    }

    public ResponseList<Group> searchGroups(String query, Reading reading) throws FacebookException {
        return factory.createGroupList(get(buildSearchURL(query, "group", reading)));
    }

    public ResponseList<Place> searchPlaces(String query) throws FacebookException {
        return searchPlaces(query, null);
    }

    public ResponseList<Place> searchPlaces(String query, Reading reading) throws FacebookException {
        return factory.createPlaceList(get(buildSearchURL(query, "place", reading)));
    }

    public ResponseList<Place> searchPlaces(String query, GeoLocation center, int distance) throws FacebookException {
        return searchPlaces(query, center, distance, null);
    }

    public ResponseList<Place> searchPlaces(String query, GeoLocation center, int distance, Reading reading) throws FacebookException {
        String url = buildSearchURL(query, "place", reading)
                        + "&center=" + center.asParameterString()
                        + "&distance=" + distance;
        return factory.createPlaceList(get(url));
    }

    public ResponseList<Checkin> searchCheckins() throws FacebookException {
        return searchCheckins(null);
    }

    public ResponseList<Checkin> searchCheckins(Reading reading) throws FacebookException {
        return factory.createCheckinList(get(buildSearchURL(null, "checkin", reading)));
    }

    public ResponseList<Location> searchLocations(GeoLocation center, int distance) throws FacebookException {
        return searchLocations(center, distance, null);
    }

    public ResponseList<Location> searchLocations(GeoLocation center, int distance, Reading reading) throws FacebookException {
        String url = buildSearchURL(null, "location", reading)
                + "&center=" + center.asParameterString()
                + "&distance=" + distance;
        return factory.createLocationList(get(url));
    }

    public ResponseList<Location> searchLocations(String placeId) throws FacebookException {
        return searchLocations(placeId, null);
    }

    public ResponseList<Location> searchLocations(String placeId, Reading reading) throws FacebookException {
        String url = buildSearchURL(null, "location", reading)
                        + "&place=" + placeId;
        return factory.createLocationList(get(url));
    }

    public ResponseList<JSONObject> search(String query) throws FacebookException {
        return search(query, null);
    }

    public ResponseList<JSONObject> search(String query, Reading reading) throws FacebookException {
        String url = buildSearchURL(query, null, reading);
        return factory.createJSONObjectList(get(url));
    }

    public ResponseList<Page> searchPages(String query) throws FacebookException {
        return searchPages(query, null);
    }

    public ResponseList<Page> searchPages(String query, Reading reading) throws FacebookException {
        String url = buildSearchURL(query, "page", reading);
        return factory.createPageList(get(url));
    }

    /* FQL Methods */
    
    public JSONArray executeFQL(String query) throws FacebookException {
        ensureAuthorizationEnabled();
        String url = "";
        try {
            url = conf.getRestBaseURL() + "fql?q=" + URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException ignore) {
        }
        JSONObject json = get(url).asJSONObject();
        try {
            return json.getJSONArray("data");
        } catch (JSONException jsone) {
            throw new FacebookException(jsone.getMessage(), jsone);
        }
    }
    
    public Map<String, JSONArray> executeMultiFQL(Map<String, String> queries) throws FacebookException {
        ensureAuthorizationEnabled();
        JSONObject json = get(conf.getRestBaseURL() + "fql?q=" + convertQueriesToJson(queries))
                          .asJSONObject();
        Map<String, JSONArray> result = new HashMap<String, JSONArray>();
        try {
            JSONArray jsonArray = json.getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String name = jsonObject.getString("name");
                JSONArray resultSets = jsonObject.getJSONArray("fql_result_set");
                result.put(name, resultSets);
            }
        } catch (JSONException jsone) {
            throw new FacebookException(jsone.getMessage(), jsone);
        }
        return result;
    }

    private String convertQueriesToJson(Map<String, String> queries) {
        List<String> jsons = new ArrayList<String>();
        for (String name : queries.keySet()) {
            String json = "";
            try {
                json = "%22" + URLEncoder.encode(name, "UTF-8") + "%22" + ":" + "%22" + URLEncoder.encode(queries.get(name), "UTF-8") + "%22";
            } catch (UnsupportedEncodingException ignore) {
            }
            jsons.add(json);
        }
        return "{" + z_F4JInternalStringUtil.join((String[]) jsons.toArray(new String[jsons.size()])) + "}";
    }
    
    /* Test User Methods */

    public TestUser createTestUser(String appId) throws FacebookException {
        return createTestUser(appId, null, null, null);
    }
    
    public TestUser createTestUser(String appId, String name, String locale, String permissions) throws FacebookException {
        ensureAuthorizationEnabled();
        String _locale = "en_US";
        if (locale != null) _locale = locale;
        return factory.createTestUser(post(conf.getRestBaseURL() + appId + "/accounts/test-users" + 
                    "?installed=true" +
                    "&name=" + HttpParameter.encode(name) +
                    "&locale=" + HttpParameter.encode(_locale) +
                    "&permissions=" + HttpParameter.encode(permissions)));
    }
    
    public List<TestUser> getTestUsers(String appId) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = get(conf.getRestBaseURL() + appId + "/accounts/test-users");
        try {
            JSONArray data = res.asJSONObject().getJSONArray("data");
            List<TestUser> testUsers = new ArrayList<TestUser>();
            for (int i = 0; i < data.length(); i++) {
                testUsers.add(factory.createTestUser(data.getJSONObject(i)));
            }
            return testUsers;
        } catch (JSONException jsone) {
            throw new FacebookException(jsone.getMessage(), jsone);
        }
    }

    public boolean deleteTestUser(String testUserId) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = delete(conf.getRestBaseURL() + testUserId);
        return Boolean.valueOf(res.asString().trim());
    }

    public boolean makeFriendTestUser(TestUser testUser1, TestUser testUser2) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = post(buildURL(testUser1.getId(), "friends/" + testUser2.getId()),
                                new HttpParameter[]{new HttpParameter("access_token", testUser1.getAccessToken())});
        if (!Boolean.valueOf(res.asString().trim())) {
            return false;
        }
        res = post(buildURL(testUser2.getId(), "friends/" + testUser1.getId()),
                                new HttpParameter[]{new HttpParameter("access_token", testUser2.getAccessToken())});
        return Boolean.valueOf(res.asString().trim());
    }
    
    /* Paging */

    @SuppressWarnings("unchecked")
    public <T> ResponseList<T> fetchNext(Paging<T> paging) throws FacebookException {
        ensureAuthorizationEnabled();
        URL url = paging.getNext();
        if (url == null) {
            return null;
        }
        return (ResponseList<T>) fetchPaging(url, paging.getJSONObjectType());
    }
    
    @SuppressWarnings("unchecked")
    public <T> ResponseList<T> fetchPrevious(Paging<T> paging) throws FacebookException {
        ensureAuthorizationEnabled();
        URL url = paging.getPrevious();
        if (url == null) {
            return null;
        }
        return (ResponseList<T>) fetchPaging(url, paging.getJSONObjectType());
    }

    private <T> ResponseList<T> fetchPaging(URL url, Class<T> jsonObjectType) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = getRaw(url.toString());
        return (ResponseList<T>) factory.createResponseList(res, jsonObjectType);
    }


    /* common methods */
    
    private ResponseList<Comment> _getComments(String objectId, Reading reading) throws FacebookException {
        return factory.createCommentList(get(buildURL(objectId, "comments", reading)));
    }

    private ResponseList<Like> _getLikes(String objectId, Reading reading) throws FacebookException {
        return factory.createLikeList(get(buildURL(objectId, "likes", reading)));
    }
    
    private URL _getPictureURL(String objectId, String connection, PictureSize size) throws FacebookException {
        String url = buildURL(objectId, connection);
        HttpResponse res;
        if (size != null) {
            res = get(url, new HttpParameter[]{new HttpParameter("type", size.toString())});
        } else {
            res = get(url);
        }
        try {
            return new URL(res.getResponseHeader("Location"));
        } catch (MalformedURLException urle) {
            throw new FacebookException(urle.getMessage(), urle);
        }
    }

    private String _comment(String objectId, String message) throws FacebookException {
        ensureAuthorizationEnabled();
        JSONObject json = post(buildURL(objectId, "comments"),
                                new HttpParameter[]{new HttpParameter("message", message)})
                          .asJSONObject();
        try {
            return json.getString("id");
        } catch (JSONException jsone) {
            throw new FacebookException(jsone.getMessage(), jsone);
        }
    }

    private boolean _like(String objectId) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = post(buildURL(objectId, "likes"));
        return Boolean.valueOf(res.asString().trim());
    }

    private boolean _unlike(String objectId) throws FacebookException {
        ensureAuthorizationEnabled();
        HttpResponse res = delete(buildURL(objectId, "likes"));
        return Boolean.valueOf(res.asString().trim());
    }

    private String _postFeed(String objectId, PostUpdate postUpdate)
            throws FacebookException {
        ensureAuthorizationEnabled();
        JSONObject json = post(buildURL(objectId, "feed"),
                            postUpdate.asHttpParameterArray()
                          ).asJSONObject();
        return getRawString("id", json);
    }

    private String _postLink(String objectId, URL link, String message)
            throws FacebookException {
        HttpParameter[] httpParameters = {new HttpParameter("link", link.toString())};
        if (message != null) {
            httpParameters = HttpParameter.merge(
                                httpParameters,
                                new HttpParameter[]{new HttpParameter("message", message)}
                             );
        }
        JSONObject json = post(buildURL(objectId, "feed"), httpParameters)
                          .asJSONObject();
        try {
            return json.getString("id");
        } catch (JSONException jsone) {
            throw new FacebookException(jsone.getMessage(), jsone);
        }
    }

    private String _postStatusMessage(String objectId, String message) throws FacebookException {
        JSONObject json = post(buildURL(objectId, "feed"),
                            new HttpParameter[] {new HttpParameter("message", message)}
                          ).asJSONObject();
        try {
            return json.getString("id");
        } catch (JSONException jsone) {
            throw new FacebookException(jsone.getMessage(), jsone);
        }
    }

    
    /* http methods */
    
    private HttpResponse get(String url) throws FacebookException {
        if (!conf.isMBeanEnabled()) {
            return http.get(url, auth);
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.get(url, auth);
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                FacebookAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }

    private HttpResponse get(String url, HttpParameter[] parameters) throws FacebookException {
        if (!conf.isMBeanEnabled()) {
            return http.get(url, parameters, (containsAccessToken(parameters) ? null : auth));
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.get(url, parameters, (containsAccessToken(parameters) ? null : auth));
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                FacebookAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }

    private HttpResponse getRaw(String url) throws FacebookException {
        if (!conf.isMBeanEnabled()) {
            return http.get(url);
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.get(url);
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                FacebookAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }

    private HttpResponse post(String url) throws FacebookException {
        if (!conf.isMBeanEnabled()) {
            return http.post(url, auth);
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.post(url, auth);
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                FacebookAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }

    private HttpResponse post(String url, HttpParameter[] parameters) throws FacebookException {
        if (!conf.isMBeanEnabled()) {
            return http.post(url, parameters, (containsAccessToken(parameters) ? null : auth));
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.post(url, parameters, (containsAccessToken(parameters) ? null : auth));
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                FacebookAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }

    private HttpResponse delete(String url) throws FacebookException {
        if (!conf.isMBeanEnabled()) {
            return http.delete(url, auth);
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.delete(url, auth);
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                FacebookAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }

    private HttpResponse delete(String url, HttpParameter[] parameters) throws FacebookException {
        if (!conf.isMBeanEnabled()) {
            return http.delete(url, (containsAccessToken(parameters) ? null : auth));
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.delete(url, parameters, (containsAccessToken(parameters) ? null : auth));
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                FacebookAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }
    

    private boolean isOk(HttpResponse response) {
        return response != null && response.getStatusCode() < 300;
    }
    
    private boolean containsAccessToken(HttpParameter[] parameters) throws FacebookException {
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getName().equals("access_token")) {
                return true;
            }
        }
        return false;
    }

}
