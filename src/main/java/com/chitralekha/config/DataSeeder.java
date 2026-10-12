package com.chitralekha.config;

import com.chitralekha.entity.*;
import com.chitralekha.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedDatabase(
            UserRepository userRepository,
            ProfileRepository profileRepository,
            ContentRepository contentRepository,
            ContentListRepository contentListRepository,
            ContentListItemRepository contentListItemRepository,
            FollowRepository followRepository,
            PostRepository postRepository,
            PostCommentRepository postCommentRepository,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            RatingRepository ratingRepository,
            ReviewRepository reviewRepository,
            UserContentActivityRepository activityRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            // Seed Users
            User arya = userRepository.save(new User("arya", "arya@chitralekha.com", passwordEncoder.encode("Password123!"), true));
            User nina = userRepository.save(new User("nina", "nina@chitralekha.com", passwordEncoder.encode("Password123!"), true));
            User dev = userRepository.save(new User("dev", "dev@chitralekha.com", passwordEncoder.encode("Password123!"), true));

            // Seed Profiles
            profileRepository.save(new Profile(arya, "Arya Roy", "Fascinated by slow cinema, nonlinear fiction, and existential cyberpunk worlds.", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80", "The Social Network", "Severance", "Cowboy Bebop", "Piranesi"));
            profileRepository.save(new Profile(nina, "Nina Kapoor", "Collector of literary fiction, meditative anime, and carefully curated weekend lists.", "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80", "Blade Runner 2049", "Succession", "Spirited Away", "The Left Hand of Darkness"));
            profileRepository.save(new Profile(dev, "Dev Sharma", "Constantly hunting for stories that refuse to follow conventional narrative formulas.", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80", "Arrival", "Dark", "Fullmetal Alchemist", "Dune"));

            // Seed Content across all 4 categories (Movies, TV Shows, Anime, Books)
            Content m1 = new Content("MOVIE", "TMDB", "550", "Fight Club", "An insomniac office worker looking for a way to change his life crosses paths with a devil-may-care soap maker.", "https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg", "https://image.tmdb.org/t/p/original/hZkgoQYus5vegHoetLkCJzb17zJ.jpg", 1999, "Drama, Thriller", "David Fincher", 139);
            Content m2 = new Content("MOVIE", "TMDB", "603", "The Matrix", "Set in the 22nd century, The Matrix tells the story of a computer hacker who joins a group of underground insurgents.", "https://image.tmdb.org/t/p/w500/f89U3ADr1oiB1s9GkdPOEpX6G5F.jpg", "https://image.tmdb.org/t/p/original/7c9UVPPiTPltouxRVY6N9uugaVA.jpg", 1999, "Sci-Fi, Action", "Lana & Lilly Wachowski", 136);
            Content m3 = new Content("MOVIE", "TMDB", "335984", "Blade Runner 2049", "Thirty years after the events of the first film, a new blade runner unearths a long-buried secret.", "https://image.tmdb.org/t/p/w500/gajva2L0rPYkEWjzgFlBXCAVBE5.jpg", "https://image.tmdb.org/t/p/original/sAtoMqDVhNDQBc3QJL3RF6hlxGq.jpg", 2017, "Sci-Fi, Drama", "Denis Villeneuve", 164);
            
            Content s1 = new Content("SHOW", "TMDB", "1399", "Game of Thrones", "Seven noble families fight for control of the mythical land of Westeros.", "https://image.tmdb.org/t/p/w500/1XS1oqL89zok8zGv2vQdJv2b5qg.jpg", "https://image.tmdb.org/t/p/original/2OMB0ynKlyIenMJWI2Dy9IWT4c.jpg", 2011, "Drama, Fantasy", "David Benioff, D.B. Weiss", 73);
            Content s2 = new Content("SHOW", "TMDB", "110492", "Severance", "Mark leads a team of office workers whose memories have been surgically divided between work and personal lives.", "https://image.tmdb.org/t/p/w500/1mdmddj0n3A3aT8xSgL45q1xU6.jpg", "https://image.tmdb.org/t/p/original/w4zC2N7lAkmkR1oE9rWwU7u5T1c.jpg", 2022, "Drama, Sci-Fi, Mystery", "Dan Erickson, Ben Stiller", 19);
            Content s3 = new Content("SHOW", "TMDB", "70523", "Dark", "A missing child sets four families on a frantic hunt for answers as they unearth a mind-bending mystery that spans three generations.", "https://image.tmdb.org/t/p/w500/apbrWgAQxD2q9OdngGfs7lT408W.jpg", "https://image.tmdb.org/t/p/original/3lBDg3i6nn5R2NKICJ4j1NqupW2.jpg", 2017, "Sci-Fi, Mystery, Crime", "Baran bo Odar, Jantje Friese", 26);

            Content a1 = new Content("ANIME", "ANILIST", "1", "Cowboy Bebop", "Enter a world in the year 2071, where humanity has expanded into the galaxy and bounty hunters track down the most elusive outlaws.", "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx1-z77g1y6U08m3.jpg", "https://s4.anilist.co/file/anilistcdn/media/anime/banner/1-O67512DrLp73.jpg", 1998, "Action, Sci-Fi, Adventure", "Shinichiro Watanabe, Sunrise", 26);
            Content a2 = new Content("ANIME", "ANILIST", "199", "Spirited Away", "During her family's move to the suburbs, a sullen 10-year-old girl wanders into a world ruled by gods, witches, and spirits.", "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx199-Biy56y4869wO.png", "https://s4.anilist.co/file/anilistcdn/media/anime/banner/199.jpg", 2001, "Adventure, Fantasy, Supernatural", "Hayao Miyazaki, Studio Ghibli", 1);
            Content a3 = new Content("ANIME", "ANILIST", "5114", "Fullmetal Alchemist: Brotherhood", "Two brothers search for a Philosopher's Stone after an attempt to revive their deceased mother goes horribly wrong.", "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx5114-1yYmG1U7771m.jpg", "https://s4.anilist.co/file/anilistcdn/media/anime/banner/5114.jpg", 2009, "Action, Adventure, Drama, Fantasy", "Bones", 64);

            Content b1 = new Content("BOOK", "GOOGLE_BOOKS", "ZL6TQgAACAAJ", "Piranesi", "Piranesi lives in the House. Perhaps he always has. In his notebooks he makes a meticulous record of its wonders.", "https://books.google.com/books/content?id=ZL6TQgAACAAJ&printsec=frontcover&img=1&zoom=1&source=gbs_api", null, 2020, "Fiction, Literary, Fantasy", "Susanna Clarke", 272);
            Content b2 = new Content("BOOK", "GOOGLE_BOOKS", "B1h0DwAAQBAJ", "Dune", "Set on the desert planet Arrakis, Dune is the story of the boy Paul Atreides, who would become the mysterious man known as Muad'Dib.", "https://books.google.com/books/content?id=B1h0DwAAQBAJ&printsec=frontcover&img=1&zoom=1&source=gbs_api", null, 1965, "Fiction, Sci-Fi", "Frank Herbert", 688);
            Content b3 = new Content("BOOK", "GOOGLE_BOOKS", "s1gVAAAAYAAJ", "The Left Hand of Darkness", "A groundbreaking work of science fiction that follows Genly Ai, a human envoy sent to the planet Gethen.", "https://books.google.com/books/content?id=s1gVAAAAYAAJ&printsec=frontcover&img=1&zoom=1&source=gbs_api", null, 1969, "Fiction, Sci-Fi, Classic", "Ursula K. Le Guin", 304);

            contentRepository.saveAll(List.of(m1, m2, m3, s1, s2, s3, a1, a2, a3, b1, b2, b3));

            // Seed Tracking Activity
            activityRepository.save(new UserContentActivity(arya, m1, "WATCHED", 139, true));
            activityRepository.save(new UserContentActivity(arya, b1, "READ", 272, true));
            activityRepository.save(new UserContentActivity(arya, a1, "WATCHED", 26, true));
            activityRepository.save(new UserContentActivity(arya, s2, "WATCHING", 8, false));

            activityRepository.save(new UserContentActivity(nina, m3, "WATCHED", 164, true));
            activityRepository.save(new UserContentActivity(nina, a2, "WATCHED", 1, true));
            activityRepository.save(new UserContentActivity(nina, b3, "READ", 304, true));
            activityRepository.save(new UserContentActivity(nina, b1, "READ", 272, false));

            activityRepository.save(new UserContentActivity(dev, m2, "WATCHED", 136, true));
            activityRepository.save(new UserContentActivity(dev, s3, "WATCHED", 26, true));
            activityRepository.save(new UserContentActivity(dev, a3, "WATCHED", 64, true));
            activityRepository.save(new UserContentActivity(dev, b2, "READING", 420, true));

            // Seed Ratings
            ratingRepository.save(new Rating(arya, m1, 9, "A sharp, unforgettable examination of modern alienation and consumer identity."));
            ratingRepository.save(new Rating(arya, b1, 10, "Pure architectural poetry. One of the greatest speculative fiction achievements."));
            ratingRepository.save(new Rating(arya, a1, 9, "A jazz symphony wrapped in space noir. Nothing else matches its atmosphere."));

            ratingRepository.save(new Rating(nina, m3, 10, "Denis Villeneuve crafts a sequel that rivals the original in contemplation and grandeur."));
            ratingRepository.save(new Rating(nina, a2, 10, "Ghibli's masterpiece. Every frame is imbued with reverence for childhood curiosity."));
            ratingRepository.save(new Rating(nina, b1, 9, "The quiet compassion of the protagonist makes the endless halls feel intimate."));

            ratingRepository.save(new Rating(dev, m2, 10, "Rewrote the grammar of action and philosophy in popular cinema."));
            ratingRepository.save(new Rating(dev, s3, 9, "The tightest time-travel narrative ever committed to television."));
            ratingRepository.save(new Rating(dev, b2, 9, "Epic in scope, political in nuance, and timeless in imagination."));

            // Seed Thoughtful Reviews
            reviewRepository.save(new Review(arya, b1, "The Solitary Splendor of the Tides", "Piranesi is a novel that trusts its reader completely. Rather than rushing to explain the mystery of the House, Susanna Clarke allows us to dwell in its beauty and respect its rhythm. The character of Piranesi himself is a gentle antidote to cynical fiction.", 10, false));
            reviewRepository.save(new Review(nina, m3, "Memory as Architecture in Blade Runner 2049", "What struck me on this rewatch is how much silence is in this film. In an era where blockbusters fear a second of stillness, Roger Deakins and Villeneuve let the dust settle over Los Angeles and Las Vegas with cathedral-like gravity.", 10, false));
            reviewRepository.save(new Review(dev, s3, "A Masterclass in Narrative Inevitability", "Dark is rare because it honors every single clue it plants across three seasons. The sound design alone carries an eerie sense of predestination. If you love stories that reward strict attention, do not miss this.", 9, false));

            // Seed Follow Relationships
            followRepository.save(new Follow(arya, nina));
            followRepository.save(new Follow(nina, arya));
            followRepository.save(new Follow(dev, arya));
            followRepository.save(new Follow(dev, nina));

            // Seed Lists
            ContentList l1 = contentListRepository.save(new ContentList(arya, "Architectural & Atmospheric Worlds", "Stories where setting itself becomes a sentient character and emotional force.", "PUBLIC"));
            contentListItemRepository.save(new ContentListItem(l1, b1, 1, "The endless halls of the House"));
            contentListItemRepository.save(new ContentListItem(l1, m3, 2, "Brutalist neon Los Angeles"));
            contentListItemRepository.save(new ContentListItem(l1, a1, 3, "Run-down space colonies and jazz cafes"));

            ContentList l2 = contentListRepository.save(new ContentList(nina, "Foundational Speculative Literature", "Novels and manga that shaped my understanding of what speculative storytelling can do.", "PUBLIC"));
            contentListItemRepository.save(new ContentListItem(l2, b3, 1, "The Left Hand of Darkness"));
            contentListItemRepository.save(new ContentListItem(l2, b2, 2, "Dune"));
            contentListItemRepository.save(new ContentListItem(l2, b1, 3, "Piranesi"));

            // Seed Posts
            Post p1 = postRepository.save(new Post(arya, b1, "Finished reading Piranesi for the second time. The way Clarke depicts solitary reverence without loneliness is something I will keep returning to."));
            postCommentRepository.save(new PostComment(p1, nina, "Could not agree more. The journals have such a distinct, honest rhythm."));

            Post p2 = postRepository.save(new Post(nina, m3, "Roger Deakins' cinematography in Blade Runner 2049 still leaves me breathless. The orange smog sequence in Las Vegas is pure cinema."));
            postCommentRepository.save(new PostComment(p2, dev, "That sequence in IMAX was an experience I will never forget."));

            Post p3 = postRepository.save(new Post(dev, s3, "Just finished rewatching Dark season 2. The loop logic is so airtight it almost hurts your brain in the best way possible."));

            // Seed 1-on-1 Conversations & Messages
            Conversation conv1 = conversationRepository.save(new Conversation(arya, nina));
            messageRepository.save(new Message(conv1, arya, nina, "Nina, have you read The Left Hand of Darkness yet? I saw it on your favorites."));
            messageRepository.save(new Message(conv1, nina, arya, "Yes! It is one of my all-time favorites. The anthropology and the winter landscape are incredible."));
        };
    }
}
