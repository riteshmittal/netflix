package com.aem.community.core.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jcr.Session;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;

@Model(adaptables = { Resource.class,
		SlingHttpServletRequest.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class MediaContainerModel {

	@SlingObject
	Resource resource;

	@ValueMapValue(name = JcrConstants.JCR_TITLE)
	String title;

	List<Resource> slides = new ArrayList<Resource>();
	private static final long serialVersionUID = 2610051404257637265L;

	@Inject
	private QueryBuilder builder;

	@Inject
	private SlingHttpServletRequest slingHttpServletRequest;

	@Inject
	private ResourceResolverFactory resolverFactory;

	private static final String SUBSERVICE = "datawrite";
	/**
	 * Session object
	 */
	private Session session;

	@PostConstruct
	protected void init() {
		if (slingHttpServletRequest.getPathInfo().contains("action")) {
			searchAssets("action");
		} else if (slingHttpServletRequest.getPathInfo().contains("thriller")) {
			searchAssets("thriller");
		} else if (slingHttpServletRequest.getPathInfo().contains("horror")) {
			searchAssets("horror");
		}

	}

	public List<Resource> getSlides() {
		return slides;
	}

	/**
	 * Gets the Resource resolver object in service.
	 *
	 * @return resourceResolver the resourceResolverObject
	 */
	public ResourceResolver getResourceResolver() {
		ResourceResolver resourceResolver = null;
		try {
			Map<String, Object> param = new HashMap<>();
			param.put(ResourceResolverFactory.SUBSERVICE, SUBSERVICE);
			resourceResolver = resolverFactory.getServiceResourceResolver(param);
		} catch (Exception e) {
		}
		return resourceResolver;
	}

	public void searchAssets(String searchWord) {
		try {
			ResourceResolver resourceResolver = getResourceResolver();
			session = resourceResolver.adaptTo(Session.class);
			Map<String, String> predicate = new HashMap<>();
			predicate.put("path", "/content/we-retail/ca/en/");
			predicate.put("tagsearch", searchWord);
			Query query = builder.createQuery(PredicateGroup.create(predicate), session);
			query.setStart(0);
			query.setHitsPerPage(20);
			SearchResult searchResult = query.getResult();

			for (Hit hit : searchResult.getHits()) {
				String path = hit.getPath() + "/root/responsivegrid/image";
				Resource resource = resourceResolver.getResource(path);
				slides.add(resource);
			}
		} catch (Exception e) {

			System.out.println("error" + e);
		} finally {
			if (session != null) {
				session.logout();
			}
		}

	}

	public boolean getHasNav() {
		return getSlides().size() > 1;
	}

	public Resource getResource() {
		return resource;
	}

	@PostConstruct
	public void setup() {

	}

	public String getTitle() {
		return title;
	}

}
