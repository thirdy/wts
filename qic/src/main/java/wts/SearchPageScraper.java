package wts;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.apache.commons.lang3.StringUtils.trim;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import wts.SearchPageScraper.SearchResultItem.Mod;

/**
 *
 * @author thirdy
 */
public class SearchPageScraper {

	private static final String regex_horizontal_whitespace = "(^\\h*)|(\\h*$)";
	private String page;

	public SearchPageScraper(String page) {
		this.page = page;
	}

	public List<SearchResultItem> parse() {
		List<SearchResultItem> searchResultItems = new LinkedList<>();
		Document doc = Jsoup.parse(page);
		
		Element content = doc.getElementById("content");

		Elements items = null;
		if (content == null) {
			items = doc.getElementsByClass("item");
		} else {
			items = content.getElementsByClass("item");
		}

//		System.out.println(items.get(86).toString());

//		System.out.println("Items");
		for (Element element : items) {
			
			SearchResultItem item = new SearchResultItem();

			item.id = element.attr("id");
			item.id = StringUtils.remove(item.id, "item-container-");
			item.seller = element.attr("data-seller");
			item.thread = element.attr("data-thread");
			item.sellerid = element.attr("data-sellerid");
			item.buyout = element.attr("data-buyout");
			item.ign = element.attr("data-ign");
			item.league = element.attr("data-league");
			item.name = element.attr("data-name");
			item.corrupted = element.getElementsByClass("corrupted").size() > 0;
			item.identified = element.getElementsByClass("item-unid").size() == 0;
			

//			System.out.println(String.format("Now parsing item id %s name %s", item.id, item.name));
			
			Element sockElem = element.getElementsByClass("sockets-raw").get(0);
			item.socketsRaw = sockElem.text();
			
			Elements accntAgeElement = element.getElementsByAttributeValue("title", "account age and highest level");
			if (accntAgeElement != null && !accntAgeElement.isEmpty()) {
				item.ageAndHighLvl = accntAgeElement.get(0).text();
			}
			
			// ----- Requirements ----- //
			Element reqElem = element.getElementsByClass("requirements").get(0);
			List<TextNode> reqNodes = reqElem.textNodes();
			for (TextNode reqNode : reqNodes) {
				// sample [ Level:&nbsp;37 ,  Strength:&nbsp;42 ,  Intelligence:&nbsp;42 ] 
				String req = reqNode.getWholeText();
				String separator = "&nbsp;";
				String reqType = trim(substringBefore(req, separator));
				switch(reqType) {
				case "Level":
					item.reqLvl = trim(substringAfter(req, separator)); 
					break;
				case "Strength":
					item.reqStr = trim(substringAfter(req, separator)); 
					break;
				case "Intelligence":
					item.reqInt = trim(substringAfter(req, separator)); 
					break;
				case "Dexterity":
					item.reqDex = trim(substringAfter(req, separator)); 
					break;
				}
			}

			// ----- Mods ----- //
			Elements itemModsElements = element.getElementsByClass("item-mods");
			if (itemModsElements != null && itemModsElements.size() > 0) {
				Element itemMods = itemModsElements.get(0);
				if (itemMods.getElementsByClass("bullet-item").size() != 0) {
					Element bulletItem = itemMods.getElementsByClass("bullet-item").get(0);
					Elements ulMods = bulletItem.getElementsByTag("ul");
					if (ulMods.size() == 2) {
						// implicit mod
						Element implicitLi = ulMods.get(0).getElementsByTag("li").get(0);
						Mod impMod = new Mod(implicitLi.attr("data-name"), implicitLi.attr("data-value"));
						item.implicitMod = impMod;
					}
					int indexOfExplicitMods = ulMods.size() - 1;
					Elements modsLi = ulMods.get(indexOfExplicitMods).getElementsByTag("li");
					for (Element modLi : modsLi) {
						// explicit mods
						Mod mod = new Mod(modLi.attr("data-name"), modLi.attr("data-value"));
						item.explicitMods.add(mod);
					}
				}
			}
			
			// ----- Properties ----- //
			// this is the third column data (the first col is the image, second is the mods, reqs)
			item.quality = element.getElementsByAttributeValue("data-name", "q").get(0).text().replaceAll(regex_horizontal_whitespace,"").trim();
			item.physDmgRangeAtMaxQuality = element.getElementsByAttributeValue("data-name", "pd").get(0).text().replaceAll(regex_horizontal_whitespace,"").trim();
			item.eleDmgRange = element.getElementsByAttributeValue("data-name", "ed").get(0).text().replaceAll(regex_horizontal_whitespace,"").trim();
			item.attackSpeed = element.getElementsByAttributeValue("data-name", "aps").get(0).text().replaceAll(regex_horizontal_whitespace,"").trim();
			item.dmgAtMaxQuality = element.getElementsByAttributeValue("data-name", "dps").get(0).text().replaceAll(regex_horizontal_whitespace,"").trim();
			item.physDmgAtMaxQuality = element.getElementsByAttributeValue("data-name", "pdps").get(0).text().replaceAll(regex_horizontal_whitespace,"").trim();
			item.eleDmg = element.getElementsByAttributeValue("data-name", "edps").get(0).text().replaceAll(regex_horizontal_whitespace,"").trim();
			item.armourAtMaxQuality = element.getElementsByAttributeValue("data-name", "armour").get(0).text().replaceAll(regex_horizontal_whitespace,"").trim();
			item.evasionAtMaxQuality = element.getElementsByAttributeValue("data-name", "evasion").get(0).text().replaceAll(regex_horizontal_whitespace,"").trim();
			item.energyShieldAtMaxQuality = element.getElementsByAttributeValue("data-name", "shield").get(0).text().replaceAll(regex_horizontal_whitespace,"").trim();
			item.block = element.getElementsByAttributeValue("data-name", "block").get(0).text().replaceAll(regex_horizontal_whitespace,"").trim();
			item.crit = element.getElementsByAttributeValue("data-name", "crit").get(0).text().replaceAll(regex_horizontal_whitespace,"").trim();
			// "level"
			item.imageUrl = element.getElementsByAttributeValue("alt", "Item icon").get(0).attr("src");
			
			Elements onlineSpans = element.getElementsMatchingText("online");
			if (!onlineSpans.isEmpty()) {
				item.online="Online";
			} else {
				item.online="";
			}
			
			searchResultItems.add(item);
		}
//		System.out.println("DONE --- Items");

		return searchResultItems;
	}

	/**
	 * Models one item in the search results
	 */
	public static class SearchResultItem {

		String id; // the id in the search result html page
		String buyout;
		String name;
		String ign;
		public boolean corrupted;
		public boolean identified;
		
		String socketsRaw;
		
		String quality;
		
		String physDmgRangeAtMaxQuality;
		String physDmgAtMaxQuality;
		String eleDmgRange;
		String attackSpeed;
		String dmgAtMaxQuality;
		String crit;
		String eleDmg;
		
		String armourAtMaxQuality;
		String evasionAtMaxQuality;
		String energyShieldAtMaxQuality;
		String block;
		
		String reqLvl;
		String reqStr;
		String reqInt;
		String reqDex;

		String ageAndHighLvl;
		String league;
		String seller;
		String thread;
		String sellerid;
		String threadUrl;
		
		String imageUrl;

		Mod implicitMod;
		List<Mod> explicitMods = new ArrayList<>();
		
		String online;
		
		public List<Mod> getExplicitMods() {
			return explicitMods;
		}
		
		public String getWTB() {
			String mods = buildWTBModsMessage();
			return String.format(
					"@%s Hi, I would like to buy your %s listed for %s in %s. With stats%s",
					getIgn(), getName(), getBuyout(), getLeague(), mods);
		}

		private String buildWTBModsMessage() {
			StringBuilder sb = new StringBuilder();
			if (implicitMod != null) {
				sb.append("--- [Implicit] " + implicitMod.toStringDisplay());
			}
			if (explicitMods.size() > 0) {
				sb.append("--- [Explicit]");
				for (Mod mod : explicitMods) {
					sb.append(" --- " + mod.toStringDisplay());
				}
			}
			return sb.toString();
		}

		/**
		 * @author thirdy
		 *
		 */
		public static class Mod {
			String name;
			String value;

			public Mod(String name, String value) {
				this.name = name;
				this.value = value;
			}
			
			public String getName() {
				return name;
			}

			public String getValue() {
				return value;
			}

			@Override
			public String toString() {
				return System.lineSeparator() + "Mod [name=" + name + ", value=" + value + "]";
			}

			/*  Convert the ff into human readable form:
			    #Socketed Gems are Supported by level # Increased Area of Effect
				##% increased Physical Damage
				#+# to Strength
				#+# to Accuracy Rating
				#+# Mana Gained on Kill
				#+# to Weapon range
			 */
			public String toStringDisplay() {
				String display = name;
				if (StringUtils.startsWith(name, "#") || StringUtils.startsWith(name, "$")) {
					display = StringUtils.removeStart(display, "#");
					display = StringUtils.removeStart(display, "$");
					String val = StringUtils.endsWith(value, ".0") ? StringUtils.substringBefore(value, ".0") : value;
					display = StringUtils.replaceOnce(display, "#", val);
				}
				return display;
			}
		}
		
		public String toDisplay(String newLine) {
			StringBuilder builder = new StringBuilder();
			String _quality  = isNotBlank(quality) ? " " + quality + "%" : "";
			String linksocks = isNotBlank(socketsRaw) ? " " + socketsRaw : "";
			String strCorrupt = corrupted ? " Corrupted " : "";
			strCorrupt += !identified ? " Unidentified " : "";
			builder.append(format("[%s] %s%s%s", id, name, linksocks, _quality));
			builder.append(format("%s -----%s------ ", newLine, strCorrupt));
			builder.append(newLine);
			if (implicitMod != null) {
				builder.append(format("%s", implicitMod.toStringDisplay()));
				builder.append(newLine + " ----------- " + newLine);
			}
			if (explicitMods.size() > 0) {
				for (Mod mod : explicitMods) {
					builder.append(format("%s", mod.toStringDisplay()));
					builder.append(newLine);
				}
				builder.append("-----------" + newLine);
			}
			String _physDmg 	= isNotBlank(physDmgAtMaxQuality) ? ("pDPS " + physDmgAtMaxQuality) : "";
			String _eleDmg 		= isNotBlank(eleDmg) 			  ? ("eDPS " + eleDmg) : "";
			String _attackSpeed = isNotBlank(attackSpeed) 		  ? ("APS " + attackSpeed) : "";
			String _crit 		= isNotBlank(crit) 				  ? ("Cc " + crit) : "";
			String offense = format("%s %s %s %s", _physDmg, _eleDmg, _attackSpeed, _crit).trim();
			offense = offense.isEmpty() ? "" : (offense + newLine);
			builder.append(offense);
			
			String _armour 		= isNotBlank(armourAtMaxQuality) 	? ("Ar " + armourAtMaxQuality) : "";
			String _evasion 	= isNotBlank(evasionAtMaxQuality) 	? ("Ev " + evasionAtMaxQuality) : "";
			String _energyShield = isNotBlank(energyShieldAtMaxQuality) ? ("Es " + energyShieldAtMaxQuality) : "";
			String _block 		= isNotBlank(block) 				? ("Bk " + block) : "";
			String defense = format("%s %s %s %s", _armour, _evasion, _energyShield, _block).trim();
			defense = defense.isEmpty() ? "" : (defense + newLine);
			builder.append(defense);
			
			builder.append(format("%s IGN: %s", buyout, ign));
			return builder.toString();
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			String lineSeparator = System.lineSeparator();
			builder.append(lineSeparator);
			builder.append("id=");
			builder.append(id);
			builder.append(lineSeparator);
			builder.append("buyout=");
			builder.append(buyout);
			builder.append(lineSeparator);
			builder.append("name=");
			builder.append(name);
			builder.append(lineSeparator);
			builder.append("corrupted=");
			builder.append(corrupted);
			builder.append(lineSeparator);
			builder.append("identified=");
			builder.append(identified);
			builder.append(lineSeparator);
			builder.append("ign=");
			builder.append(ign);
			builder.append(lineSeparator);
			builder.append("socketsRaw=");
			builder.append(socketsRaw);
			builder.append(lineSeparator);
			builder.append("quality=");
			builder.append(quality);
			builder.append(lineSeparator);
			builder.append("physDmgRangeAtMaxQuality=");
			builder.append(physDmgRangeAtMaxQuality);
			builder.append(lineSeparator);
			builder.append("physDmgAtMaxQuality=");
			builder.append(physDmgAtMaxQuality);
			builder.append(lineSeparator);
			builder.append("eleDmgRange=");
			builder.append(eleDmgRange);
			builder.append(lineSeparator);
			builder.append("attackSpeed=");
			builder.append(attackSpeed);
			builder.append(lineSeparator);
			builder.append("dmgAtMaxQuality=");
			builder.append(dmgAtMaxQuality);
			builder.append(lineSeparator);
			builder.append("crit=");
			builder.append(crit);
			builder.append(lineSeparator);
			builder.append("eleDmg=");
			builder.append(eleDmg);
			builder.append(lineSeparator);
			builder.append("armourAtMaxQuality=");
			builder.append(armourAtMaxQuality);
			builder.append(lineSeparator);
			builder.append("evasionAtMaxQuality=");
			builder.append(evasionAtMaxQuality);
			builder.append(lineSeparator);
			builder.append("energyShieldAtMaxQuality=");
			builder.append(energyShieldAtMaxQuality);
			builder.append(lineSeparator);
			builder.append("block=");
			builder.append(block);
			builder.append(lineSeparator);
			builder.append("reqLvl=");
			builder.append(reqLvl);
			builder.append(lineSeparator);
			builder.append("reqStr=");
			builder.append(reqStr);
			builder.append(lineSeparator);
			builder.append("reqInt=");
			builder.append(reqInt);
			builder.append(lineSeparator);
			builder.append("reqDex=");
			builder.append(reqDex);
			builder.append(lineSeparator);
			builder.append("ageAndHighLvl=");
			builder.append(ageAndHighLvl);
			builder.append(lineSeparator);
			builder.append("league=");
			builder.append(league);
			builder.append(lineSeparator);
			builder.append("seller=");
			builder.append(seller);
			builder.append(lineSeparator);
			builder.append("thread=");
			builder.append(thread);
			builder.append(lineSeparator);
			builder.append("sellerid=");
			builder.append(sellerid);
			builder.append(lineSeparator);
			builder.append("threadUrl=");
			builder.append(threadUrl);
			builder.append(lineSeparator);
			builder.append("imageUrl=");
			builder.append(imageUrl);
			builder.append(lineSeparator);
			builder.append("implicitMod=");
			builder.append(implicitMod);
			builder.append(lineSeparator);
			builder.append("explicitMods=");
			builder.append(explicitMods);
			builder.append(lineSeparator);
			builder.append("online=");
			builder.append(online);
			builder.append(lineSeparator);
			return builder.toString();
		}

		public String getId() {
			return id;
		}

		public String getBuyout() {
			return buyout;
		}

		public String getName() {
			return name;
		}

		public String getIgn() {
			return ign;
		}

		public String getSocketsRaw() {
			return socketsRaw;
		}

		public String getQuality() {
			return quality;
		}

		public String getPhysDmgRangeAtMaxQuality() {
			return physDmgRangeAtMaxQuality;
		}

		public String getEleDmgRange() {
			return eleDmgRange;
		}

		public String getAttackSpeed() {
			return attackSpeed;
		}

		public String getDmgAtMaxQuality() {
			return dmgAtMaxQuality;
		}

		public String getPhysDmgAtMaxQuality() {
			return physDmgAtMaxQuality;
		}

		public String getEleDmg() {
			return eleDmg;
		}

		public String getArmourAtMaxQuality() {
			return armourAtMaxQuality;
		}

		public String getEvasionAtMaxQuality() {
			return evasionAtMaxQuality;
		}

		public String getEnergyShieldAtMaxQuality() {
			return energyShieldAtMaxQuality;
		}

		public String getBlock() {
			return block;
		}

		public String getCrit() {
			return crit;
		}

		public String getReqLvl() {
			return reqLvl;
		}

		public String getReqStr() {
			return reqStr;
		}

		public String getReqInt() {
			return reqInt;
		}

		public String getReqDex() {
			return reqDex;
		}

		public String getAgeAndHighLvl() {
			return ageAndHighLvl;
		}

		public String getLeague() {
			return league;
		}

		public String getSeller() {
			return seller;
		}

		public String getThread() {
			return thread;
		}

		public String getSellerid() {
			return sellerid;
		}

		public String getThreadUrl() {
			return threadUrl;
		}

		public Mod getImplicitMod() {
			return implicitMod;
		}
		
		public String getOnline() {
			return online;
		}

		public void setOnline(String online) {
			this.online = online;
		}
		
		public String getImageUrl() {
			return imageUrl;
		}

		public String getPseudoEleResistance() {
			return getExplicitModValueByName("#(pseudo) +#% total Elemental Resistance");
		}

		public String getPseudoLife() {
			return getExplicitModValueByName("#(pseudo) (total) +# to maximum Life");
		}
		
		public String getFireRes() {
			return getExplicitModValueByName("#+#% to Fire Resistance");
		}
		
		public String getColdRes() {
			return getExplicitModValueByName("#+#% to Cold Resistance");
		}
		
		public String getLightRes() {
			return getExplicitModValueByName("#+#% to Light Resistance");
		}
		
		private String getExplicitModValueByName(String name) {
			for (Mod mod : explicitMods) {
				if (mod.getName().equalsIgnoreCase(name)) {
					return mod.getValue();
				}
			}
			return "";
		}

		/**
		 * Used for showing exceptions in the result table.
		 */
		public static SearchResultItem exceptionItem(Exception e) {
			SearchResultItem item = new SearchResultItem();
			item.name = e.getMessage();
			item.ign = e.getClass().getName();
			item.socketsRaw = e.getCause().getMessage();
			return item;
		}

		public static SearchResultItem message(String message) {
			SearchResultItem item = new SearchResultItem() {
				@Override
				public String getWTB() {
					return message;
				}
			};
			return item;
		}
		
	}

}