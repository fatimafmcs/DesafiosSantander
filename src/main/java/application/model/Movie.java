package application.model;

import java.util.ArrayList;
import java.util.List;

public class Movie {

	private String name;
	private List<String> directors;
	private List<String> protagonists;
	private Integer releaseYear;

	public Movie () {
		this.name = "";
		this.directors = new ArrayList<String>();
		this.protagonists = new ArrayList<String>();
		this.releaseYear = 0;
	}

	public Movie(String name, List<String> directors, List<String> protagonists, int releaseYear) {
		this.name = name;
		this.directors = directors;
		this.protagonists = protagonists;
		this.releaseYear = releaseYear;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getDirectors() {
		return directors;
	}

	public void setDirectors(List<String> directors) {
		this.directors = directors;
	}

	public List<String> getProtagonists() {
		return protagonists;
	}

	public void setProtagonists(List<String> protagonists) {
		this.protagonists = protagonists;
	}

	public Integer getReleaseYear() {
		return releaseYear;
	}

	public void setReleaseYear(Integer releaseYear) {
		this.releaseYear = releaseYear;
	}

}

