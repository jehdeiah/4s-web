package com.googlecode._4s_web.server;

import com.googlecode._4s_web.server.entity.Character;
import com.googlecode._4s_web.server.entity.Event;
import com.googlecode._4s_web.server.entity.EventRelation;
import com.googlecode._4s_web.server.entity.Information;
import com.googlecode._4s_web.server.entity.Knowledge;
import com.googlecode._4s_web.server.entity.Perception;
import com.googlecode._4s_web.server.entity.Story;
import com.googlecode._4s_web.server.entity.StoryTime;
import com.googlecode._4s_web.server.entity.Impact;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

public class OfyService {
	static {
		factory().register(Story.class);
		factory().register(Event.class);
		factory().register(Character.class);
		factory().register(StoryTime.class);
		factory().register(Information.class);
		factory().register(Knowledge.class);
		factory().register(Perception.class);
		factory().register(Impact.class);
		factory().register(EventRelation.class);
	}

	public static Objectify ofy() {
		return ObjectifyService.ofy();
	}

	public static ObjectifyFactory factory() {
		return ObjectifyService.factory();
	}
}
