package com.example.projectnailsschedule.presentation.procedures

import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.projectnailsschedule.R
import com.example.projectnailsschedule.databinding.SelectUnifBinding
import com.example.projectnailsschedule.domain.models.ProcedureModelDb
import com.example.projectnailsschedule.utils.rustore.RuStoreAd
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ProceduresFragment : Fragment() {
    private val log = this::class.simpleName
    private val proceduresViewModel: ProceduresViewModel by activityViewModels()

    private var _binding: SelectUnifBinding? = null
    private val binding get() = _binding!!

    private var proceduresList: MutableList<ProcedureModelDb>? = null
    private var proceduresRVAdapter: ProceduresRvAdapter? = null

    private var searchView: SearchView? = null
    private var recyclerView: RecyclerView? = null
    private var addButton: FloatingActionButton? = null
    private var snackbar: Snackbar? = null

    @Inject
    lateinit var ruStoreAd: RuStoreAd

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = SelectUnifBinding.inflate(inflater, container, false)

        initViews()

        swipeToDelete()

        initClickListeners()

        clearSearchView()

        proceduresViewModel.updateUserData("$log ${object{}.javaClass.enclosingMethod?.name}")

        return binding.root
    }

    private fun initViews() {
        searchView = binding.searchView
        recyclerView = binding.recyclerView
        addButton = binding.floatingActionButton
    }

    private fun initClickListeners() {
        // search panel listener
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // search only after button Search pressed on keyboard
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val searchQuery = "%$newText%"
                lifecycleScope.launch(Dispatchers.IO) {
                    proceduresList =
                        async { proceduresViewModel.searchProcedure(searchQuery) }.await()
                    withContext(Dispatchers.Main) {
                        inflateRecyclerView(proceduresList!!)
                    }
                }
                return false
            }
        })

        val searchCloseButtonId =
            searchView?.findViewById<View>(androidx.appcompat.R.id.search_close_btn)?.id
        if (searchCloseButtonId != null) {
            searchView?.findViewById<ImageView>(searchCloseButtonId)?.setOnClickListener {
                clearSearchView()
                snackbar?.dismiss()
            }
        }

        // add new procedure
        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_nav_procedures_to_nav_procedure_edit
            )
        }
    }

    private fun inflateRecyclerView(proceduresList: List<ProcedureModelDb>) {
        // create adapter
        proceduresRVAdapter = ProceduresRvAdapter(
            proceduresList = proceduresList
        )

        val layoutManager: RecyclerView.LayoutManager = GridLayoutManager(activity, 1)

        recyclerView?.layoutManager = layoutManager
        recyclerView?.adapter = proceduresRVAdapter

        // set clickListener on proceduresRV
        proceduresRVAdapter?.setOnItemClickListener(object :
            ProceduresRvAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                // edit selected procedure
                proceduresViewModel.selectedProcedure = ProcedureModelDb(
                    _id = proceduresList[position]._id,
                    procedureName = proceduresList[position].procedureName,
                    procedurePrice = proceduresList[position].procedurePrice,
                    procedureNotes = proceduresList[position].procedureNotes
                )
                findNavController().navigate(R.id.action_nav_procedures_to_nav_procedure_edit)
            }
        })
    }

    private fun swipeToDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // this method is called when the item is moved.
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val deleteProcedure: ProcedureModelDb = proceduresList!![viewHolder.adapterPosition]

                // delete client from Db
                lifecycleScope.launch(Dispatchers.IO) {
                    proceduresViewModel.deleteProcedure(deleteProcedure)
                    withContext(Dispatchers.Main) {
                        proceduresList!!.removeAt(position)
                        proceduresRVAdapter?.notifyItemRemoved(position)
                    }
                }

                // show Snackbar
                snackbar = Snackbar.make(
                    recyclerView!!, requireContext().getString(
                        R.string.procedure_deleted, deleteProcedure.procedureName
                    ), Snackbar.LENGTH_LONG
                ).setBackgroundTint(resources.getColor(R.color.yellow))
                    .setActionTextColor(resources.getColor(R.color.black))
                    .setTextColor(resources.getColor(R.color.black)).setAction(
                        getString(R.string.cancel)
                    ) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            proceduresViewModel.insertProcedure(deleteProcedure)
                            withContext(Dispatchers.Main) {
                                proceduresList!!.add(position, deleteProcedure)
                                proceduresRVAdapter?.notifyItemInserted(position)
                                recyclerView!!.smoothScrollToPosition(position)
                            }
                        }
                    }
                snackbar!!.show()
            }

            override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
                return super.getSwipeEscapeVelocity(defaultValue) * 10
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val deleteIcon: Drawable? =
                    ContextCompat.getDrawable(requireContext(), R.drawable.baseline_delete_24)!!

                val itemView = viewHolder.itemView
                val iconMarginVertical =
                    (viewHolder.itemView.height - deleteIcon!!.intrinsicHeight) / 2

                val colorDrawableBackground = ColorDrawable(resources.getColor(R.color.yellow))

                val left =
                    itemView.right - deleteIcon.intrinsicWidth - deleteIcon.intrinsicWidth // 882
                val right = itemView.right - deleteIcon.intrinsicWidth // 1014
                val top = itemView.top + iconMarginVertical
                val bottom = itemView.bottom - iconMarginVertical

                colorDrawableBackground.setBounds(
                    itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom
                )
                deleteIcon.setBounds(left, top, right, bottom)

                deleteIcon.level = 0

                colorDrawableBackground.draw(c)
                c.save()

                if (dX > 0) c.clipRect(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                else c.clipRect(
                    itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom
                )

                deleteIcon.draw(c)
                c.restore()

                super.onChildDraw(
                    c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive
                )
            }
        }).attachToRecyclerView(recyclerView)
    }

    private fun clearSearchView() {
        searchView?.setQuery(null, true)
    }

    override fun onResume() {
        super.onResume()
        ruStoreAd.banner(requireContext(), binding.root, lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        snackbar?.dismiss()
        proceduresViewModel.updateUserData("$log ${object{}.javaClass.enclosingMethod?.name}")
        _binding = null
    }
}