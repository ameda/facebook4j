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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.net.URL;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CheckinMethodsTest extends FacebookTestBase {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
    private String checkin1() throws Exception {
        String place = "100404700021921";
        GeoLocation coordinates = new GeoLocation(35.675272122419, 139.69321689514);
        CheckinCreate checkin = new CheckinCreate(place, coordinates);
        return facebook1.checkin(checkin);
    }
    
    private String checkin2() throws Exception {
        String place = "154470644580235";
        GeoLocation coordinates = new GeoLocation(35.678745360759, 139.76759590553);
        String tags = null;
        String message = "test message";
        URL link = new URL("http://www.facebook.com/");
        URL picture = null;
        CheckinCreate checkin = new CheckinCreate(place, coordinates, tags, message, link, picture);
        return facebook1.checkin(checkin);
    }
    
    @Test
    public void create() throws Exception {
        String checkinId = checkin1();
        assertThat(checkinId, is(notNullValue()));
    }

    @Test(expected = FacebookException.class)
    public void createByOtherUser() throws Exception {
        String place = "100404700021921";
        GeoLocation coordinates = new GeoLocation(35.675272122419, 139.69321689514);
        CheckinCreate checkin = new CheckinCreate(place, coordinates);
        facebook2.checkin(id1.getId(), checkin);
    }
    
    @Test
    public void createWithLink() throws Exception {
        String checkinId = checkin2();
        assertThat(checkinId, is(notNullValue()));
    }

    @Test
    public void get() throws Exception {
        String checkinId = checkin1();
        
        Checkin checkin = facebook1.getCheckin(checkinId);
        assertThat(checkin, is(notNullValue()));
        assertThat(checkin.getId(), is(checkinId));
        
        //read by other user
        Checkin checkin2 = facebook2.getCheckin(checkinId, new Reading().fields("id"));
        assertThat(checkin2, is(notNullValue()));
        assertThat(checkin2.getId(), is(checkinId));
    }
    
    @Test
    public void gets() throws Exception {
        checkin1();

        ResponseList<Checkin> checkins = facebook1.getCheckins();
        assertThat(checkins.size(), is(1));

        //read by other user
        assertThat(facebook2.getCheckins(id1.getId()).size(), is(1));
    }

    @Test
    public void comment() throws Exception {
        String checkinId = checkin1();
        
        String commentId = facebook1.commentCheckin(checkinId, "This is comment for a checkin");
        assertThat(commentId, is(notNullValue()));
        
        //read comment
        ResponseList<Comment> comments = facebook1.getCheckinComments(checkinId);
        assertThat(comments.size(), is(1));
        
        //read by other user
        ResponseList<Comment> comments2 = facebook2.getCheckinComments(checkinId);
        assertThat(comments2.size(), is(1));
    }

    @Test
    public void like() throws Exception {
        String checkinId = checkin1();
        
        //like
        boolean likeResult = facebook1.likeCheckin(checkinId);
        assertThat(likeResult, is(true));

        //read likes
        ResponseList<Like> likes = facebook1.getCheckinLikes(checkinId);
        assertThat(likes.size(), is(1));

        //unlike
        boolean unlikeResult = facebook1.unlikeCheckin(checkinId);
        assertThat(unlikeResult, is(true));

        assertThat(facebook1.getCheckinLikes(checkinId).size(), is(0));
    }
    
}
