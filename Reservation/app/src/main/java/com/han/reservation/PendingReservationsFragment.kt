package com.han.reservation

class PendingReservationsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReservationAdapter
    private val reservationList = mutableListOf<Reservation>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reservation_list, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ReservationAdapter(reservationList)
        recyclerView.adapter = adapter

        loadReservations()
        return view
    }

    private fun loadReservations() {
        val dbRef = FirebaseDatabase.getInstance().getReference("reservations")
        val guideId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        dbRef.orderByChild("guideId").equalTo(guideId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    reservationList.clear()
                    for (child in snapshot.children) {
                        val reservation = child.getValue(Reservation::class.java)
                        if (reservation != null && reservation.status == "pending") {
                            reservationList.add(reservation)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}