package cc.mi.login.db;

public abstract class DBState {
	private State curr = State.DEFAULT;
	
	public void notifyInsert() {
		this.curr = State.INSERT;
	}
	
	public void notifyUpdate() {
		if (this.curr == State.DEFAULT) {
			this.curr = State.UPDATE;
		}
	}
	
	public void notifyDelete() {
		this.curr = State.DELETE;
	}
	
	public void saved() {
		this.curr = State.DEFAULT;
	}
	
	public boolean needInsert() {
		return this.curr == State.INSERT;
	}
	
	public boolean needUpdate() {
		return this.curr == State.UPDATE;
	}
	
	public boolean needDelete() {
		return this.curr == State.DELETE;
	}
	
	public State getState() {
		return curr;
	}
	
	public abstract Object getMappinData();
}

enum State {
	DEFAULT,
	INSERT,
	UPDATE,
	DELETE;
}