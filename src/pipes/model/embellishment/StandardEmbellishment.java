package pipes.model.embellishment;

import pipes.model.Note;

public class StandardEmbellishment extends EmbellishmentFamily {

	public String getName() {
		return name;
	}

	public boolean canEmbellish(Note noteBefore, Note embellishedNote) {
		return true;
	}

	public Embellishment getEmbellishment(Note noteBefore, Note embellishedNote) {
		return model.clone();
	}
	
	public StandardEmbellishment(String name, String shortName, Embellishment model) {
		super(name, shortName);
		this.model = model;
	}

	private String name;
	private Embellishment model;
}
