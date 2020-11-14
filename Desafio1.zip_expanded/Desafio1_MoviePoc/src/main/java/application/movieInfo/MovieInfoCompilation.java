package application.movieInfo;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import application.entity.Movie;

public class MovieInfoCompilation{

	//lista unica com os filmes de todas as fontes
	List<Movie> movieListCompilation;

	// credenciais API themoviedb
	private final String TMDB_API_URL = "http://api.themoviedb.org/";
	private final String TMDB_API_KEY = "b8837d02522fd74d2a462226eb912eaf";

	public MovieInfoCompilation (String name) throws IOException  {

		List<Movie> moviesList = new ArrayList<Movie>();
		moviesList.addAll(tmdbMovieList(name));
		moviesList.addAll(mockMovieList(name));
		moviesList.addAll(mockMovieList2(name));

		movieListCompilation = new ArrayList<Movie>();
		movieListCompilation = movieListCompilation(moviesList);
	}

	/**
	 * Retorna uma lista de filmes com a participação de um determinado ator ou realizador
	 * A API da TMDB não retorna atores nem realizadores na informação do filme
	 * @param name - nome do ator ou realizador a pesquisar
	 * @return
	 * @throws IOException
	 */
	private List<Movie> tmdbMovieList (String name) throws IOException {

		List <Movie> movList = new ArrayList <Movie>();

		//url usado para pesquisar pelo ator/realizador inserido
		URL url = new URL(TMDB_API_URL + "3/search/person?query=" + name + "&api_key=" + TMDB_API_KEY);
		System.out.println(url);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();

		con.setDoOutput(true);
		con.setRequestMethod("GET");
		con.setRequestProperty("Content-Type", "application/json");
		int status = con.getResponseCode();
		StringBuilder sb = new StringBuilder();

		switch (status) {
		case 200:
		case 201:
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line+"\n");
			}
			br.close();
		}

		//obtenção da resposta JSON 
		String jsonString = sb.toString();
		JSONObject obj = new JSONObject(jsonString);

		//se não houver resultados na resposta, retorna lista vazia
		if (obj.getJSONArray("results").length() < 1) {
			return movList;
		}

		JSONObject obj2 = (JSONObject)obj.getJSONArray("results").get(0);

		//os filmes estão no parâmetro "known_for"
		JSONArray movieList = obj2.getJSONArray("known_for");

		for (int i = 0; i < movieList.length(); i++) {

			//obter apenas resultados com media_type=movie
			if (movieList.getJSONObject(i).has("media_type") && "movie".equals(movieList.getJSONObject(i).getString("media_type"))) {
				Movie mov = new Movie();

				//o nome do filme pode vir nos parâmetros original_title ou original_name
				String movieName = (movieList.getJSONObject(i).has("original_title")? 
						movieList.getJSONObject(i).getString("original_title"): movieList.getJSONObject(i).getString("original_name"));

				mov.setName(movieName);

				//obter o ano de lançamento a partir da release_date
				String releaseDate = (movieList.getJSONObject(i).has("release_date")? 
						movieList.getJSONObject(i).getString("release_date"): null);

				if (releaseDate != null) {
					Calendar cal = Calendar.getInstance();
					Date date = new Date();
					try {
						date = new SimpleDateFormat("yyyy-MM-dd").parse(releaseDate);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					cal.setTime(date);
					mov.setReleaseYear(cal.get(Calendar.YEAR));
				}

				movList.add(mov);
			}
		}

		return movList;
	}

	/**
	 * mock de lista de filmes 
	 * @param name
	 * @return
	 */
	private List<Movie> mockMovieList(String name) {

		List<Movie> moviesList = new ArrayList<Movie>();

		List<String> protagonists1 = new ArrayList<String>();
		protagonists1.add("Leonardo Dicaprio");
		protagonists1.add("Kate Winslet");

		List<String> directors1 = new ArrayList<String>();
		directors1.add("James Cameron");

		List<String> protagonists2 = new ArrayList<String>();
		protagonists2.add("Britney Spears");

		List<String> directors2 = new ArrayList<String>();
		directors2.add("Tamra Davis");

		Movie movie1 = new Movie ("Titanic",directors1, protagonists1, 1997) ;
		Movie movie2 = new Movie ("Crossroads",directors2, protagonists2, 2002) ;

		moviesList.add(movie1);
		moviesList.add(movie2);

		return moviesList;

	}


	/**
	 * mock de lista de filmes
	 * @param name
	 * @return
	 */
	private List<Movie> mockMovieList2(String name) {

		List<Movie> moviesList = new ArrayList<Movie>();

		List<String> protagonists1 = new ArrayList<String>();
		protagonists1.add("Leonardo Dicaprio");
		protagonists1.add("Kate Winslet");

		List<String> directors1 = new ArrayList<String>();
		directors1.add("James Cameron");
		directors1.add("cenas");

		List<String> protagonists2 = new ArrayList<String>();
		protagonists2.add("Britney Spears");
		protagonists2.add("cenas");

		List<String> directors2 = new ArrayList<String>();
		directors2.add("Tamra Davis");

		Movie movie1 = new Movie ("Titanic",directors1, protagonists1, 1997) ;
		Movie movie2 = new Movie ("Crossroads",directors2, protagonists2, 2002) ;

		moviesList.add(movie1);
		moviesList.add(movie2);

		return moviesList;

	}

	/**
	 * 
	 * @param moviesList - lista que contém todos os filmes obtidos das diferentes fontes de informação de filmes
	 * @return lista única de filmes, sem titulos de filme duplicados e com as listas de protagonistas e realizadores compiladas 
	 * @throws IOException
	 */
	private List<Movie> movieListCompilation(List<Movie> moviesList) throws IOException {

		List <Movie> movListFinal = new ArrayList<Movie>();

		//variavel que verifica se um filme com um determinado nome existe na lista de filmes a ser retornada
		Boolean movieExists = false;

		//percorre a lista de todos os filmes recebidos
		for (Movie mov: moviesList) {
			movieExists = false;

			//percorre a lista de filmes a ser retornada
			for (Movie mov2: movListFinal) {
				//se nome do filme 'mov' já existe na lista 'movListFinal', os seus campos são verificados e, se necessário, atualizados
				if (mov2.getName().equals(mov.getName())) {

					//junta, sem duplicados, a lista de protagonistas e realizadores dos dois filmes a ser comparados, e atualiza o filme na lista 'movListFinal'
					movListFinal.get((movListFinal.lastIndexOf(mov2))).setProtagonists(getListWithUniqueElements(mov.getProtagonists(), mov2.getProtagonists()));
					movListFinal.get((movListFinal.lastIndexOf(mov2))).setDirectors(getListWithUniqueElements(mov.getDirectors(), mov2.getDirectors()));

					//se o ano de lançamento ainda não foi definido, esse campo é atualizado
					if (movListFinal.get((movListFinal.lastIndexOf(mov2))).getReleaseYear() == 0) {
						movListFinal.get((movListFinal.lastIndexOf(mov2))).setReleaseYear(mov.getReleaseYear());
					}
					movieExists = true;
				}
			}

			//apenas adiciona um novo filme se não existe na lista final um filme com o mesmo nome
			if (!movieExists) {
				movListFinal.add(mov);
			}
		}
		return movListFinal;
	}

	/**
	 * concatena duas listas de Strings, sem duplicados
	 * @param list
	 * @param list2
	 * @return
	 */
	private List<String> getListWithUniqueElements (List<String> list, List<String> list2) {

		List<String> lists = new ArrayList<String>(list);
		lists.removeAll(list2);
		lists.addAll(list2);

		return lists;
	}

	public List<Movie> getMovieListCompilation() {
		return movieListCompilation;
	}

	public void setMovieListCompilation(List<Movie> movieListCompilation) {
		this.movieListCompilation = movieListCompilation;
	}

}