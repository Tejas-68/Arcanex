package com.procollegia.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingSource;
import androidx.paging.PagingState;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import kotlin.coroutines.Continuation;

/**
 * Paging 3 PagingSource for Attendance Logs.
 * Fetches 15 records at a time using Firestore cursor pagination (startAfter).
 * Note: Paging 3's loadPage() is a suspend function but from Java we implement it
 * via a blocking Tasks.await() call on a background thread — this is safe
 * because Paging 3 always calls load() on a background executor.
 */
public class AttendancePagingSource extends PagingSource<DocumentSnapshot, Map<String, Object>> {

    private static final int PAGE_SIZE = 15;
    private final FirebaseFirestore db;
    private final String studentId;

    public AttendancePagingSource(FirebaseFirestore db, String studentId) {
        this.db = db;
        this.studentId = studentId;
    }

    @Nullable
    @Override
    public DocumentSnapshot getRefreshKey(@NonNull PagingState<DocumentSnapshot, Map<String, Object>> state) {
        return null;
    }

    @Nullable
    @Override
    public Object load(@NonNull LoadParams<DocumentSnapshot> params, @NonNull Continuation<? super LoadResult<DocumentSnapshot, Map<String, Object>>> continuation) {
        try {
            Query query = db.collection("attendance_logs")
                    .whereEqualTo("studentId", studentId)
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(PAGE_SIZE);

            if (params.getKey() != null) {
                query = query.startAfter(params.getKey());
            }

            QuerySnapshot snap = Tasks.await(query.get());

            List<Map<String, Object>> data = new ArrayList<>();
            DocumentSnapshot lastDoc = null;
            for (DocumentSnapshot doc : snap.getDocuments()) {
                data.add(doc.getData());
                lastDoc = doc;
            }

            DocumentSnapshot nextKey = snap.size() < PAGE_SIZE ? null : lastDoc;
            return new LoadResult.Page<>(data, null, nextKey);
        } catch (Exception e) {
            return new LoadResult.Error<>(e);
        }
    }
}
