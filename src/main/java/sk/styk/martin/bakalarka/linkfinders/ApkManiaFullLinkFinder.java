package sk.styk.martin.bakalarka.linkfinders;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Styk
 */
public class ApkManiaFullLinkFinder implements ApkLinkFinder {

    private static final String APPS_PAGE = "http://apkmaniafull.com/category/android-apps/page/";
    private static final int NUMBER_OF_PAGES = 417;
    private static final String DOWNLOAD_LINK_TEXT = "Download APK from secure server >>";
    private static final String DOWNLOAD_LINK_TEXT_1 = "Download APK from secure source >>";
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(AndroidApksFreeLinkFinder.class);
    private int numberOfApks;

    private Set<String> treasure = new HashSet<>();

    @Override
    public List<String> findLinks() {
        for (int i = 1; i < NUMBER_OF_PAGES; i++) {
            if (treasure.size() >= numberOfApks) {
                break;
            }
            logger.info("processing list page " + i);
            processListPage(APPS_PAGE + i + "/");
        }
        logger.info("number of links found on pages " + treasure.size());
        return new ArrayList<>(treasure);
    }

    @Override
    public void setMetadataFile(File metadataFile) {
        if (metadataFile != null)
            throw new UnsupportedOperationException("not supported for this link finder");
    }

    @Override
    public void setNumberOfApks(int numberOfApks) {
        this.numberOfApks = numberOfApks;
    }

    private void processListPage(String url) {

        Document document = null;
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException ex) {
            Logger.getLogger(AndroidApksFreeLinkFinder.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        List<Element> pagesLinks = document.getElementsByAttributeValue("class", "more-link");
        for (Element el : pagesLinks) {
            processDetailPage(el.attr("href"));
            if (treasure.size() >= numberOfApks) {
                return;
            }
        }
    }

    private void processDetailPage(String url) {
        logger.info("processing detail page " + url);
        Document document = null;
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException ex) {
            Logger.getLogger(AndroidApksFreeLinkFinder.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        List<Element> redirectLinks = document.getElementsByAttributeValue("target", "_blank");
        for (Element link : redirectLinks) {
            String href = link.attr("href");
            if (href.contains("http://apkmaniafull.com/redirect.php?url=")) {
                String toAdd = href.substring(href.indexOf('=') + 1);
                treasure.add(toAdd);
                break;
            }
        }
    }
}
