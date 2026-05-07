@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;

    private final EntityManager em;

    // TRANSACTION STARTS
    @Transactional
    public void createUser() {

        User user = new User();
        user.setName("Chaitanya");

        Order order1 = new Order();
        order1.setProductName("Laptop");

        Order order2 = new Order();
        order2.setProductName("Phone");

        // relationship setup
        user.addOrder(order1);
        user.addOrder(order2);

        // CascadeType.ALL
        // saves orders automatically
        userRepo.save(user);

        // user is MANAGED here

        user.setName("Updated Name");

        // Dirty checking:
        // no explicit save required
    }

    @Transactional
    public void lazyLoadingExample(Long id) {

        User user = userRepo.findById(id).get();

        // ONLY user loaded initially

        // orders query runs HERE
        user.getOrders().size();
    }

    @Transactional
    public void nPlusOneSolved(Long id) {

        // JOIN FETCH avoids N+1
        User user =
            userRepo.findUserWithOrders(id).get();

        // orders already loaded
        user.getOrders().size();
    }

    @Transactional
    public void detachExample(Long id) {

        User user = em.find(User.class, id);

        // user managed

        em.detach(user);

        // now DETACHED

        user.setName("Will NOT update DB");
    }

    @Transactional
    public void mergeExample() {

        User detached = new User();
        detached.setId(1L);
        detached.setName("Merged Name");

        // merge returns MANAGED entity
        User managed = em.merge(detached);

        managed.setName("Updated Again");
    }
}
