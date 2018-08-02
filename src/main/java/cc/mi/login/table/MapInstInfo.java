package cc.mi.login.table;

public class MapInstInfo {
	private static int indxGenerator = 0;
	
	private final int indx;
	private final int instId;
	private final int parentId;
	private final int lineNo;
	private final int sceneConn;
	private final String ext;
	
	public MapInstInfo(
			int indx,
			int instId,
			int parentId,
			int lineNo,
			int sceneConn,
			String ext
			) {
		
		this.indx		= indx;
		this.instId 	= instId;
		this.parentId 	= parentId;
		this.lineNo 	= lineNo;
		this.sceneConn	= sceneConn;
		this.ext		= ext;
	}
	
	public static int newIndx() {
		return ++indxGenerator;
	}

	public int getInstId() {
		return instId;
	}

	public int getParentId() {
		return parentId;
	}

	public int getLineNo() {
		return lineNo;
	}

	public int getSceneConn() {
		return sceneConn;
	}

	public String getExt() {
		return ext;
	}

	public int getIndx() {
		return indx;
	}
}
