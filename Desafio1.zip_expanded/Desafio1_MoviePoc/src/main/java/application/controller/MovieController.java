package application.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.poi.xslf.usermodel.SlideLayout;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import application.entity.Movie;
import application.form.MovieForm;
import application.movieInfo.MovieInfoCompilation;

@Controller
public class MovieController {

	private List<Movie> movies;

	@PostConstruct
	public void initialize() {
		movies = new ArrayList<Movie>();
	}

	/**
	 * página inicial da aplicação, com inputs para pesquisar filmes por ator/realizador
	 * @param model
	 * @return
	 */
	@RequestMapping(value= {"/index","/"}, method= RequestMethod.GET)
	public String home(Model model) {

		model.addAttribute("movieForm", new MovieForm());

		return "index";
	}

	/**
	 * Método chamado quando o botão de pesquisa de um determinado ator/realizador é clicado
	 * @param form
	 * @param actor
	 * @param director
	 * @param model
	 * @return
	 */
	@RequestMapping(value= "/results", method= RequestMethod.GET)
	public ModelAndView results(@ModelAttribute MovieForm form,  @RequestParam(value = "actor", required = false) String actor,
			@RequestParam(value = "director", required = false) String director, Model model) {

		ModelAndView mav = setModelView (actor, director, form);
		model.addAttribute("movieForm", form);

		String name = mav.getModelMap().get("name").toString();
		name = name.replaceAll(" ", "+").toLowerCase();

		if (name != null && !"".equals(name)) {
			try {
				MovieInfoCompilation movies = new MovieInfoCompilation(name);
				setMovies(movies.getMovieListCompilation());

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		model.addAttribute("moviesList", getMovies());

		return mav;
	}

	/**
	 * Método chamado quando o botão "Criar Powerpoint" é clicado
	 * @param form
	 * @param actor
	 * @param director
	 * @param model
	 * @return
	 */
	@RequestMapping(value= "/results", method= RequestMethod.POST)
	public ModelAndView generatePowerpoint(@ModelAttribute MovieForm form,  @RequestParam(value = "actor", required = false) String actor,
			@RequestParam(value = "director", required = false) String director, Model model) {

		ModelAndView mav = setModelView (actor, director, form);
		String name = mav.getModelMap().get("name").toString();
		String role = mav.getModelMap().get("role").toString();

		model.addAttribute("moviesList", getMovies());

		try {
			generatePowerpoint(getMovies(),name,role);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return mav;
	}

	/**
	 * método que gera um documento Powerpoint com os filmes encontrados.
	 * O primeiro slide tem informações sobre o ator/realizador pesquisado. 
	 * Os slides seguintes apresentam informações sobre cada filme
	 * @param movies
	 * @param name
	 * @param role
	 * @throws IOException
	 */
	private void generatePowerpoint (List<Movie> movies, String name, String role) throws IOException {

		XMLSlideShow ppt = new XMLSlideShow();
		XSLFSlideMaster slideMaster = ppt.getSlideMasters().get(0);
		XSLFSlideLayout slidelayout = slideMaster.getLayout(SlideLayout.TITLE_AND_CONTENT);
		XSLFSlide slide = ppt.createSlide(slidelayout);

		XSLFTextShape body = slide.getPlaceholder(1);
		body.clearText();

		//primeiro slide, com informação sobre o ator/realizador pesquisado
		XSLFTextParagraph paragraph=body.addNewTextParagraph();
		XSLFTextRun run = paragraph.addNewTextRun();
		run.setText("Filmes com a participação do " + role + " " + name);
		run.setFontSize(40.0);
		run.setBold(true);
		paragraph.addLineBreak();

		//por cada filme encontrado, cria um slide com as informações desse filme
		for (Movie mov : movies) {
			slide = ppt.createSlide(slidelayout);

			body = slide.getPlaceholder(1);
			body.clearText();

			paragraph=body.addNewTextParagraph();
			run = paragraph.addNewTextRun();
			run.setText("Filme: " + mov.getName());
			paragraph.addLineBreak();

			run = paragraph.addNewTextRun();
			run.setText("Realizadores: " + String.join(", ", mov.getDirectors()));
			paragraph.addLineBreak();

			run = paragraph.addNewTextRun();
			run.setText("Ano de lançamento: " + mov.getReleaseYear());
			paragraph.addLineBreak();
		}

		//cria o ficheiro powerpoint, localizado na mesma pasta que o pom.xml
		File file=new File("MoviesPresentation.pptx");
		FileOutputStream out = new FileOutputStream(file);

		ppt.write(out);
		ppt.close();
		out.close();
	}

	/**
	 * define as variáveis name e role, usados na página de resultados e no powerpoint
	 * @param actor
	 * @param director
	 * @param form
	 * @return
	 */
	private ModelAndView setModelView (String actor, String director, MovieForm form) {
		ModelAndView mav = new ModelAndView();

		if (actor != null) {
			mav.addObject("name", form.getActor());
			mav.addObject("role", "Ator");
		}

		else if (director != null) {
			mav.addObject("name", form.getDirector());
			mav.addObject("role", "Realizador");
		}

		mav.setViewName("search/results");

		return mav;
	}

	public List<Movie> getMovies() {
		return movies;
	}

	public void setMovies(List<Movie> movies) {
		this.movies = movies;
	}
}