package org.doubleshow.lucene_web;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.doubleshow.lucene_web.model.DpcDao;
import org.doubleshow.lucene_web.model.DpcDaoException;
import org.doubleshow.lucene_web.model.DpcDoc;
import org.doubleshow.lucene_web.model.DpcSearchResult;
import org.doubleshow.lucene_web.model.impl.DpcDaoImpl;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;


public class Servlet1 extends HttpServlet {
	// parameter names
	static final String P_RELOAD_TEMPLATES = "reloadTemplates";
	static final String P_HITS_PER_PAGE = "hitsPerPage";
	static final String P_MAX_PAGES = "maxPages";
	static final String P_TEMPLATES_URL = "templatesURL";

	private STGroupFile stg;
	private int hitsPerPage;;
	private int maxPages;
	private URL templatesUrl;
	private boolean reloadTemplates;
	private DpcDao dao;
	List<Integer> pages;
	private boolean useStemmer;

	String getConfigValue(ServletConfig config, String name, String def) {
		String v;
		v = System.getProperty(Dpc.SP_PREFIX + name);
		if (v == null)
			v = config.getInitParameter(name);
		if (v == null)
			v = def;
		return v;
	}

	/**
	 * init DAO, templates and variables
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		try {
			String v;
			v = getConfigValue(config, Dpc.P_USE_STEMMER, "false");
			useStemmer = (v.equalsIgnoreCase("true") || v.equalsIgnoreCase("on"));
			
			v = getConfigValue(config, Dpc.P_INDEX, Dpc.DEFAULT_INDEX);
			dao = new DpcDaoImpl(v,useStemmer);

			v = getConfigValue(config, P_TEMPLATES_URL, "/WEB-INF/view/templates/dpc-default.stg");
			templatesUrl = getServletContext().getResource(v);
			stg = new STGroupFile(templatesUrl, "utf-8", '$', '$');

			v = getConfigValue(config, P_RELOAD_TEMPLATES, "false");
			reloadTemplates = (v.equalsIgnoreCase("true") || v.equalsIgnoreCase("on"));

			v = getConfigValue(config, P_HITS_PER_PAGE, "20");
			hitsPerPage = Integer.parseInt(v);

			v = getConfigValue(config, P_MAX_PAGES, "20");
			maxPages = Integer.parseInt(v);

			// init page numbering, we're reusing it
			pages = new ArrayList<Integer>();
			for (int i = 1; i <= maxPages; i++)
				pages.add(i);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException("initialization error:" + e.toString(), e);
		}
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		STGroupFile stg = reloadTemplates ? new STGroupFile(templatesUrl, "utf-8", '$', '$') : this.stg;
		List<String> messages = new ArrayList<String>();
		String s;
		int page = 1;
		SearchOptsImpl searchOpts;
		DpcSearchResult searchResult;

		// check if request to show document details, if so: render and return
		s = req.getParameter("showDocId");
		if (s != null && !(s = s.trim()).equals("")) {
			int id = Integer.parseInt(s);
			DpcDoc doc;
			try {
				doc = dao.getDoc(id);
			} catch (DpcDaoException e) {
				throw new ServletException(e);
			}
			if ((s = req.getParameter("ajax")) != null && s.trim().equalsIgnoreCase("on"))
				resp.getWriter().print(stg.getInstanceOf("documentDetails_ajax")
						.add("doc", doc)
						.add("callback", req.getParameter("callback"))
						.render()
						);
			else
				resp.getWriter().print(
						stg.getInstanceOf("main")
								.add("title", "details: " + doc.getTitle())
								.add("body", stg.getInstanceOf("documentDetails")
										.add("doc", doc)
									).render());
			return;
		}

		// get requested page number
		s = req.getParameter("p");
		if (s != null && !(s = s.trim()).equals(""))
			try {
				page = Integer.parseInt(s);
				page = Math.max(page, 1);
				page = Math.min(page, maxPages);
			} catch (Exception e) {
				System.err.println(e);
			}

		// create default search options
		searchOpts = new SearchOptsImpl("", true, false, false, false, false, (page - 1) * hitsPerPage, hitsPerPage);
		// fill them with parameters values
		optsFromParametersMap(req.getParameterMap(), searchOpts);

		// make a query
		try {
			searchResult = dao.getTop(searchOpts);
		} catch (DpcDaoException e) {
			throw new ServletException(e);
		}
		// add message
		messages.add(String.format("page %d, search time %d ms, total hits %d", page, searchResult.getSearchTime(),
				searchResult.getTotalHits()));

		// recalculate max pages with search results
		int maxP = Math.max(1, searchResult.getTotalHits() / hitsPerPage);
		maxP = Math.min(maxP, maxPages);

		// get main template
		ST st_main = stg.getInstanceOf("main");
		// fill it
		st_main.add("title", "search dpc")
				.add("body", stg.getInstanceOf("search")
						.add("docs", searchResult.getDocs())
						.add("searchOpts", searchOpts)
						.add("messages", messages)
						.add("currentQuery", req.getQueryString() != null ? req.getQueryString().replaceFirst("&p=\\d+", "") : null)
						.add("pages", pages.subList(0, maxP))
					);

		// render
		resp.getWriter().print(st_main.render());
	}

	public SearchOptsImpl optsFromParametersMap(Map<String, String[]> map, SearchOptsImpl i) {
		for (String k : map.keySet())
			if (k.equals("q"))
				i.setQ(map.get(k)[0].trim());
			else if (k.equals("mode"))
				i.setAll(map.get(k)[0].equals("all"));
			else if (k.equals("f_title"))
				i.setF_title(map.get(k)[0].equals("on"));
			else if (k.equals("f_description"))
				i.setF_description(map.get(k)[0].equals("on"));
			else if (k.equals("f_categories"))
				i.setF_categories(map.get(k)[0].equals("on"));
			else if (k.equals("f_comment"))
				i.setF_comment(map.get(k)[0].equals("on"));
		return i;
	}
}
