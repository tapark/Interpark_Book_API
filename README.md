# 인터파크 도서검색(API)

### retrofit2
API 연동 시 유용한 기능을 제공해주는 라이브러리
~~~kotlin
// build.gradle 에 추가
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
// json(string) -> gson(object) converter
~~~
interface 생성 -> 외부 API(open API) 와 http 통신
~~~kotlin
// BookService.kt 생성
interface BookService {

	// 검색 api 이고 json 형식으로(= 고정값)
    @GET("/api/search.api?output=json")
    fun getBooksByName(
        @Query("key") apiKey: String,
        @Query("query") keyword: String
    ): Call<SearchBookDto>
	// 베스트셀러 api 이고 json 형식과 카테고리 100(국내도서)으로(= 고정값)
    @GET("/api/bestSeller.api?output=json&categoryId=100")
    fun getBestSellerBooks(
        @Query("key") apiKey: String
    ): Call<BestSellerDto> // data class BestSellerDto를 호출
}
~~~
data class와 @SerializedName 으로 json key-value date에 접근
~~~kotlin
// SearchBookDto.kt 생성
data class SearchBookDto(
	// [in json] title : "인터파크도서검색결과"
    @SerializedName("title") val title: String,
	// [in json] item이라는 key값에 도서목록이 List로 존재함
    @SerializedName("item") val bookList: List<Book>
)

// Book.kt 생성
data class Book(
	// List의 개별 oject를 정의(id, 제목, 설명, 이미지)
    @SerializedName("itemId") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("coverSmallUrl") val coverSmallUrl: String
)
~~~
retrofit 생성 및 데이터 접근
~~~kotlin
// retrofit 생성
val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL) // 고정 URL
    .addConverterFactory(GsonConverterFactory.create())
    .build()
// retrofit, interface(BookService) 연결
val bookService = retrofit.create(BookService::class.java)

// getBestSellerBooks : http @GET 호출하여 enqueue로 통신
bookService.getBestSellerBooks(API_KEY)
    .enqueue(object: Callback<BestSellerDto> {
        override fun onResponse(
            call: Call<BestSellerDto>,
            response: Response<BestSellerDto>
        ) {
            if (response.isSuccessful.not()) {
                return
            }
            //response.body() 로 BestSellerDto 데이터에 접근
            response.body()?.let {
                Log.d(TAG, it.toString())

                it.bookList.forEach { list ->
                    Log.d(TAG, list.toString())
                }
                // adapter에 BestSellerDto.bookList 전달
                adapter.submitList(it.bookList)
            }
        }
~~~

### RecyclerView (ListAdapter, ViewHolder, diffUtil)
다수의 List가 존재할때 개별 Component 마다 View를 생성하는 것이 아닌 생성된 View에 데이터만 교체하여 View를 재활용 하는 방식
~~~kotlin
// BookAdapter.kt 생성
class BookAdapter: ListAdapter<Book, BookAdapter.BooKItemViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BooKItemViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val view = ItemBookBinding.inflate(layoutInflater, parent, false)

        return BooKItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: BooKItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class BooKItemViewHolder(
        private val binding: ItemBookBinding
    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(bookModel: Book) {
            binding.titleTextView.text = bookModel.title
            binding.descriptionTextView.text = bookModel.description

            Glide
                .with(binding.coverImageView)
                .load(bookModel.coverSmallUrl)
                .into(binding.coverImageView)

        }
    }
    // diffUtil : 선별적으로 업데이트 하기위한
    companion object {
        val diffUtil = object: DiffUtil.ItemCallback<Book>() {
            override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
                return oldItem.id == newItem.id
            }

        }
    }
}
~~~

### ViewBinding
View를 보다 쉽게 호출 findViewById와 대체가능
~~~kotlin
// build.gradle에 viewBinding 추가
android {
    viewBinding {
        enabled = true
    }
}
// MainActivity.kt 에서 activity_main.xml 객체를 호출하려면?
// activity_main 파스칼식으로 표기 + Binding = ActivityMainBinding
private lateinit var binding: ActivityMainBinding
binding = ActivityMainBinding.inflate(layoutInflater)

// binding의 필드로 해당 layout의 TextView와 Button에 접근 가능
binding.nameTextView.text = viewModel.name
binding.okButton.setOnClickListener { }
~~~

### Glide : Image Loading Library
~~~kotlin
// build.gradle에 추가
implementation 'com.github.bumptech.glide:glide:4.12.0'
// image url을 통해 imageView에 추가 할 수있다. -> 매우 편리함
Glide
    .with(binding.coverImageView)
    .load(bookModel.coverSmallUrl)
    .into(binding.coverImageView)
~~~

### intent.putExtra 로 data class 넘겨주기(parcelize)
~~~kotlin
// build.gradle에 추가
plugins {
    id 'kotIin-parcelize'
}
// Book.kt 에 @Parcelize, Parcelable
@Parcelize
data class Book(
    @SerializedName("itemId") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("coverSmallUrl") val coverSmallUrl: String,
    @SerializedName("coverLargeUrl") val coverLargeUrl: String
): Parcelable
~~~

### local Database ROOM
~~~kotlin
// build.gradle에 추가
plugins {
    id 'kotIin-kapt'
}
dependencies {
    implementation 'androidx.room:room-runtime:2.2.6'
    kapt 'androidx.room:room-compiler:2.2.6'
}

// History.kt 생성 -> database Table(Entity)
@Entity(tableName = "history")
data class History(
    @PrimaryKey val uid: Int?,
    @ColumnInfo(name = "db_keyword") val keyword: String?
)

// HistoryDao.kt 생성 -> database를 조작할 쿼리문을 함수로 작성
@Dao
interface HistoryDao {

    @Query("SELECT * FROM history")
    fun getAll(): List<History>

    @Insert
    fun insertHistory(history: History)

    @Query("DELETE FROM history WHERE db_keyword == :keyword")
    fun delete(keyword: String)
}

// AppDatabase.kt 생성 -> 전체 데이터베이스 구조
@Database(entities = [History::class, Review::class], version = 2)
abstract class AppDatabase: RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun reviewDao(): ReviewDao
}
// History::class 가 version 1이었고 Review::class를 추가하여 version 2로 생성하려면? -> migrate
// 추가된 Review::class를 쿼리문으로 table 및 column 생성
fun getAppDatabase(context: Context): AppDatabase {

    val migration1To2 = object: Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE `REVIEW` (`id` INTEGER, `review` TEXT, PRIMARY KEY(`id`))")
        }
    }
    // Room 생성 시 .addMigrations(migration1To2)
    return Room.databaseBuilder(context, AppDatabase::class.java, "BookSearchDB")
        .addMigrations(migration1To2).build()
}
~~~