package org.superbiz.moviefun;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.GetMapping;
import org.superbiz.moviefun.albums.Album;
import org.superbiz.moviefun.albums.AlbumFixtures;
import org.superbiz.moviefun.albums.AlbumsBean;
import org.superbiz.moviefun.movies.Movie;
import org.superbiz.moviefun.movies.MovieFixtures;
import org.superbiz.moviefun.movies.MoviesBean;

import java.util.Map;

@Controller
public class HomeController {

    private final MoviesBean moviesBean;
    private final AlbumsBean albumsBean;
    private final MovieFixtures movieFixtures;
    private final AlbumFixtures albumFixtures;
    private final PlatformTransactionManager albumsTransactionManager;
    private final PlatformTransactionManager moviesTransactionManager;

    public HomeController(
            MoviesBean moviesBean,
            AlbumsBean albumsBean,
            MovieFixtures movieFixtures,
            AlbumFixtures albumFixtures,
            @Qualifier("albumsTransactionManager") PlatformTransactionManager albumsTransactionManager,
            @Qualifier("moviesTransactionManager") PlatformTransactionManager moviesTransactionManager ) {

        this.moviesBean = moviesBean;
        this.albumsBean = albumsBean;
        this.movieFixtures = movieFixtures;
        this.albumFixtures = albumFixtures;
        this.albumsTransactionManager = albumsTransactionManager;
        this.moviesTransactionManager = moviesTransactionManager;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/setup")
    public String setup(Map<String, Object> model) {

        DefaultTransactionDefinition moviesTxDef = new DefaultTransactionDefinition();
        moviesTxDef.setName("moviesTx");
        moviesTxDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus moviesTx = moviesTransactionManager.getTransaction(moviesTxDef);

        try {
            moviesBean.clean();

            for (Movie movie : movieFixtures.load()) {
                moviesBean.addMovie(movie);
            }

            moviesTransactionManager.commit(moviesTx);
        }
        catch (Exception ex) {
            moviesTransactionManager.rollback(moviesTx);
            throw ex;
        }


        DefaultTransactionDefinition albumsTxDef = new DefaultTransactionDefinition();
        moviesTxDef.setName("albumsTx");
        moviesTxDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus albumsTx = albumsTransactionManager.getTransaction(moviesTxDef);

        try {
            albumsBean.clean();

            for (Album album : albumFixtures.load()) {
                albumsBean.addAlbum(album);
            }
            albumsTransactionManager.commit(albumsTx);
        }
        catch (Exception ex) {
            albumsTransactionManager.rollback(albumsTx);
            throw ex;
        }

        model.put("movies", moviesBean.getMovies());
        model.put("albums", albumsBean.getAlbums());

        return "setup";
    }
}
