package script;

import java.util.ArrayList;

import org.dom4j.Document;

import sun.util.resources.cldr.nd.LocaleNames_nd;

public class HistoricalChat {
	private String[] chats;
	private long[] ids;
	
	public HistoricalChat() {
		chats = new String[0];
		ids = new long[0];
	}
	public boolean isLowerThan(HistoricalChat chat) {
		if(chats.length == 0) {
			return false;
		}
		if(ids[0] >= chat.getIds()[chat.getIds().length -1]) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public boolean isHigherThan(HistoricalChat chat) {
		if(chat.getIds().length == 0) {
			return false;
		}
		if(ids[ids.length -1] > chat.getIds()[0]) {
			return false;
		}
		else {
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
	public void append(String xml) {
		Document document = null;
		try {
			document = XmlLoader.loadFromString(xml);
		} catch (InterruptedException e) {
			e.printStackTrace();
			//TODO
		}
		org.dom4j.Element root = document.getRootElement();
		ArrayList<org.dom4j.Element> list = (ArrayList<org.dom4j.Element>) root.elements("d");
		ArrayList<String> chats1 = new ArrayList<>();
		ArrayList<Long> ids1 = new ArrayList<>();
		for (int j = 0; j < ids.length; j++) {
			chats1.add(chats[j]);
			ids1.add(ids[j]);
		}
		for (int j = 0; j < list.size(); j++) {
			chats1.add(list.get(j).getText());
			String[] p = list.get(j).attributeValue("p").split(",");
			ids1.add(Long.parseLong(p[7]));
		}
		chats = chats1.toArray(new String[0]);
		ids = new long[ids1.size()];
		for(int i = 0; i < ids.length; i ++) {
			ids[i]= ids1.get(i); 
		}
	}
}
