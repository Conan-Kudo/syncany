package org.syncany.config.to;

import java.io.File;

import javax.crypto.spec.SecretKeySpec;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Commit;
import org.simpleframework.xml.core.Complete;
import org.simpleframework.xml.core.Persist;
import org.simpleframework.xml.core.Persister;
import org.syncany.config.Config.ConfigException;
import org.syncany.crypto.SaltedSecretKey;
import org.syncany.util.StringUtil;

@Root(name="config")
@Namespace(reference="http://syncany.org/config/1")
public class ConfigTO {
	@Element(name="machinename", required=true)
	private String machineName;

	@Element(name="masterkey", required=false)
	private String masterKeyEncoded;
	private SaltedSecretKey masterKey;
	
	@Element(name="connection", required=true)
	private ConnectionTO connectionTO;

	public static ConfigTO load(File file) throws ConfigException {
		try {
			return new Persister().read(ConfigTO.class, file);
		}
		catch (Exception ex) {
			throw new ConfigException("Config file does not exist or is invalid: " + file, ex);
		}
	}

	public static void save(ConfigTO configTO, File file) throws Exception {
		Serializer serializer = new Persister();
		serializer.write(configTO, file);
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public ConnectionTO getConnectionTO() {
		return connectionTO;
	}

	public void setConnection(ConnectionTO connectionTO) {
		this.connectionTO = connectionTO;
	}
	
	public SaltedSecretKey getMasterKey() {
		return masterKey;
	}

	public void setMasterKey(SaltedSecretKey masterKey) {
		this.masterKey = masterKey;
	}

	@Persist
	public void prepare() {
		if (masterKey != null) {
			masterKeyEncoded = StringUtil.toHex(masterKey.getSalt())+"/"+StringUtil.toHex(masterKey.getEncoded());
		}
		else {
			masterKeyEncoded = null;
		}
	}

	@Complete
	public void release() {
		masterKeyEncoded = null;
	}
	
	@Commit
	public void commit() {
		if (masterKeyEncoded != null && !"".equals(masterKeyEncoded)) {
			String[] masterKeyEncodedParts = masterKeyEncoded.split("/");
			
			byte[] saltBytes = StringUtil.fromHex(masterKeyEncodedParts[0]);
			byte[] masterKeyBytes = StringUtil.fromHex(masterKeyEncodedParts[1]);
			
			masterKey = new SaltedSecretKey(new SecretKeySpec(masterKeyBytes, "RAW"), saltBytes);
		}
		else {
			masterKey = null;
		}
	}

	public static class ConnectionTO extends TypedPropertyListTO {
		// Nothing special about this
	}
}
