package script;

import java.util.ArrayList;

import org.dom4j.Document;

public class HistoricalChat {
	private String[] chats;
	private long[] ids;
	private int max_limit = 0;

	public HistoricalChat() {
		chats = new String[0];
		ids = new long[0];
	}

	public boolean isLowerThan(HistoricalChat chat) {
		if (chat.getIds().length < chat.max_limit) {
			return false;
		}
		if (chats.length == 0) {
			return true;
		}
		if (ids[0] >= chat.getIds()[chat.getIds().length - 1]) {
			return false;
		} else {
			return true;
		}
	}

	public String[] getChats() {
		return chats;
	}

	public void setChats(String[] chats) {
		this.chats = chats;
	}

	public long[] getIds() {
		return ids;
	}

	public void setIds(long[] ids) {
		this.ids = ids;
	}

	public void append(String xml) throws InterruptedException, NumberFormatException {
		Document document = null;
		document = XmlLoader.loadFromString(xml);
		org.dom4j.Element root = document.getRootElement();
		ArrayList<org.dom4j.Element> list = (ArrayList<org.dom4j.Element>) root.elements("d");
		ArrayList<org.dom4j.Element> limit = (ArrayList<org.dom4j.Element>) root.elements("maxlimit");
		max_limit = Integer.parseInt(limit.get(0).getStringValue());
		ArrayList<String> chats1 = new ArrayList<>();
		ArrayList<Long> ids1 = new ArrayList<>();
		for (int j = 0; j < ids.length; j++) {
			chats1.add(chats[j]);
			ids1.add(ids[j]);
		}
		for (int j = 0; j < list.size(); j++) {
			String[] p = list.get(j).attributeValue("p").split(",");
			chats1.add("<d p=\"" + list.get(j).attributeValue("p") + "\">"
					+ list.get(j).getStringValue().replace("<", "&lt;").replace("&", "&amp;") + "</d>");
			ids1.add(Long.parseLong(p[7]));
		}
		chats = chats1.toArray(new String[0]);
		ids = new long[ids1.size()];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = ids1.get(i);
		}
	}
}
