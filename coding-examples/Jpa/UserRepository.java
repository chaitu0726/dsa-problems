@Repository
public interface UserRepository
    extends JpaRepository<User, Long> {

    // JPQL
    @Query("""
       SELECT u
       FROM User u
       JOIN FETCH u.orders
       WHERE u.id = :id
    """)
    Optional<User> findUserWithOrders(Long id);
}
