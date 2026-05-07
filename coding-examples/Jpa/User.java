@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // LAZY avoids loading all orders automatically
    @OneToMany(
        mappedBy = "user",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL
    )
    private List<Order> orders = new ArrayList<>();

    // helper method
    public void addOrder(Order order) {
        orders.add(order);
        order.setUser(this);
    }

    // getters/setters
}
