package at.tugraz.iaik.las.p2.ttp.client.data;

import java.util.Date;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.view.client.ProvidesKey;

/***
 * Represents a single Tag out in the wild with its associated UID and public
 * key to run the protocol.
 * 
 * Remark: I'm breaking the OO paradigm of getters/setters on purpose to make
 * the code more readable. The getter/setters would anyway simply get/set the
 * value without any further code.
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
@PersistenceCapable(detachable = "true")
public class TagJdo implements IsSerializable {

	/**
	 * The key provider that provides the unique ID.
	 */
	@NotPersistent
	public static final ProvidesKey<TagJdo> KEY_PROVIDER = new ProvidesKey<TagJdo>() {
		public Object getKey(TagJdo item) {
			return item == null ? null : item.Key;
		}
	};

	// recommended to specify persistent/nonpersistent always
	// https://developers.google.com/appengine/docs/java/datastore/jdo/dataclasses

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	@Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
	public String Key;

	@Persistent
	public String Name;

	@Persistent
	public String Description;

	@Persistent
	public Date CreationDate;

	/**
	 * Uid is stored as string, not as byte array to be able to make a JDO query
	 * for it.
	 */
	@Persistent
	public String Uid;

	@Persistent
	public Double LocationLatitude;

	@Persistent
	public Double LocationLongitude;

	@Persistent(defaultFetchGroup = "true")
	public FileJdo PublicKeyCertificateFile;

	// public Key getKey() {
	// return key;
	// }
	//
	// public void setKey(Key key) {
	// this.key = key;
	// }
	//
	// public String getName() {
	// return name;
	// }
	//
	// public void setName(String name) {
	// this.name = name;
	// }
	//
	// public String getDescription() {
	// return description;
	// }
	//
	// public void setDescription(String description) {
	// this.description = description;
	// }
	//
	// public Date getCreationDate() {
	// return creationDate;
	// }
	//
	// public void setCreationDate(Date creationDate) {
	// this.creationDate = creationDate;
	// }
	//
	// public TagDto toTag() {
	// TagDto tag = new TagDto();
	// tag.CreationDate = this.creationDate;
	// tag.Description = this.description;
	// tag.Name = this.name;
	// tag.Key = KeyFactory.keyToString(this.key);
	// return tag;
	// }
	//
	// public static TagJdo createFromTag(TagDto tag) {
	// TagJdo tagJdo = new TagJdo();
	// tagJdo.setCreationDate(tag.CreationDate);
	// tagJdo.setDescription(tag.Description);
	// tagJdo.setName(tag.Name);
	// tagJdo.setKey(KeyFactory.stringToKey(tag.Key));
	// return tagJdo;
	// }
}
