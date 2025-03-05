package com.jetpack.menubar

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.BounceInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.MenuRes
import androidx.appcompat.view.SupportMenuInflater
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuItemImpl
import androidx.core.view.isGone



@SuppressLint("RestrictedApi")
class FoldingTabBar : LinearLayout {

    private val ANIMATION_DURATION = 500L
    private val START_DELAY = 150L
    private val MAIN_ROTATION_START = 0f
    private val MAIN_ROTATION_END = 405f
    private val ITEM_ROTATION_START = 180f
    private val ITEM_ROTATION_END = 360f
    private val ROLL_UP_ROTATION_START = -45f
    private val ROLL_UP_ROTATION_END = 360f

    var onFoldingItemClickListener: OnFoldingItemSelectedListener? = null
    var onMainButtonClickListener: OnMainButtonClickedListener? = null

    private var mData: List<SelectedMenuItem> = emptyList()

    private var mExpandingSet: AnimatorSet = AnimatorSet()
    private var mRollupSet: AnimatorSet = AnimatorSet()
    private var isAnimating: Boolean = false

    private var mMenu: MenuBuilder

    private var mSize: Int = 0
    private var indexCounter = 0
    private var mainImageView: ImageView = ImageView(context)
    private var selectedIndex: Int = 0

    private var itemsPadding: Int = 0
    private var drawableResource: Int = 0
    private var selectionColor: Int = 0

    var isShowing: Boolean = false
    var isRecording: Boolean = false
    var isPausing: Boolean = false

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleRes: Int)
            : super(context, attrs, defStyleRes) {
        mMenu = MenuBuilder(context)
        gravity = Gravity.CENTER

        if (background == null) {
            setBackgroundResource(R.drawable.background_tabbar)
        }
        val a: TypedArray = initAttrs(attrs, defStyleRes)

        mSize = getSizeDimension()
        initViewTreeObserver(a)
    }

    /**
     * Initializing attributes
     */
    private fun initAttrs(attrs: AttributeSet?, defStyleRes: Int) =
        context.obtainStyledAttributes(
            attrs,
            R.styleable.FoldingTabBar, 0,
            defStyleRes
        )

    /**
     * Here is size of our FoldingTabBar. Simple
     */
    private fun getSizeDimension(): Int = resources.getDimensionPixelSize(R.dimen.ftb_size_mini)

    /**
     * This is the padding for menu items
     */
    private fun getItemsPadding(): Int = resources.getDimensionPixelSize(R.dimen.ftb_item_padding)

    /**
     * When folding tab bar pre-draws we should initialize
     * inflate our menu, and also add menu items, into the
     * FoldingTabBar, also here we are initializing animators
     * and animation sets
     */
    private fun initViewTreeObserver(a: TypedArray) {
        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                viewTreeObserver.removeOnPreDrawListener(this)
                isAnimating = true
                initAttributesValues(a)
                initExpandAnimators()
                initRollUpAnimators()
                return true
            }
        })
    }

    /**
     * Here we are initializing default values
     * Also here we are binding new attributes into this values
     *
     * @param a - incoming typed array with attributes values
     */
    private fun initAttributesValues(a: TypedArray) {
        drawableResource = R.drawable.ic_action_plus
        itemsPadding = getItemsPadding()
        selectionColor = R.color.ftb_selected_dot_color
        if (a.hasValue(R.styleable.FoldingTabBar_mainImage)) {
            drawableResource = a.getResourceId(R.styleable.FoldingTabBar_mainImage, 0)
        }
        if (a.hasValue(R.styleable.FoldingTabBar_itemPadding)) {
            itemsPadding = a.getDimensionPixelSize(R.styleable.FoldingTabBar_itemPadding, 0)
        }
        if (a.hasValue(R.styleable.FoldingTabBar_selectionColor)) {
            selectionColor = a.getResourceId(R.styleable.FoldingTabBar_selectionColor, 0)
        }
        if (a.hasValue(R.styleable.FoldingTabBar_menu)) {
            inflateMenu(a.getResourceId(R.styleable.FoldingTabBar_menu, 0))
        } else {
            inflateMenu(R.menu.menu_control_record)
        }
    }

    /**
     * Expand animation. Whole animators
     */
    private fun initExpandAnimators() {
        mExpandingSet.duration = ANIMATION_DURATION

        val destWidth = childCount.times(mSize)

        val rotationSet = AnimatorSet()
        val scalingSet = AnimatorSet()

        val scalingAnimator = ValueAnimator.ofInt(0, destWidth).apply {
            addUpdateListener(scaleAnimator)
            addListener(rollUpListener)
        }

        val rotationAnimator = ValueAnimator.ofFloat(MAIN_ROTATION_START, MAIN_ROTATION_END).apply {
            addUpdateListener { valueAnimator ->
                val value = valueAnimator.animatedValue as Float
                mainImageView.rotation = value
            }
        }

        mData.forEach { item ->
            ValueAnimator.ofFloat(ITEM_ROTATION_START, ITEM_ROTATION_END).apply {
                addUpdateListener {
                    val fraction = it.animatedFraction
                    item.scaleX = fraction
                    item.scaleY = fraction
                    item.rotation = it.animatedValue as Float
                }
                addListener(expandingListener)
                rotationSet.playTogether(this)
            }
        }

        scalingSet.playTogether(scalingAnimator, rotationAnimator)
        scalingSet.interpolator = CustomBounceInterpolator()
        rotationSet.interpolator = BounceInterpolator()

        rotationSet.startDelay = START_DELAY
        //mExpandingSet.playTogether(scalingSet, rotationSet)
        //mExpandingSet.playTogether(rotationSet)
        mExpandingSet.playTogether(rotationSet)
    }

    /**
     * Roll-up animators. Whole roll-up animation
     */
    private fun initRollUpAnimators() {
        mRollupSet.duration = ANIMATION_DURATION

        val destWidth = mMenu.size().times(mSize)

        val rotationSet = AnimatorSet()

        val scalingAnimator = ValueAnimator.ofInt(destWidth, 0)
        val rotationAnimator = ValueAnimator.ofFloat(ROLL_UP_ROTATION_START, ROLL_UP_ROTATION_END)

        scalingAnimator.addUpdateListener(scaleAnimator)
        mRollupSet.addListener(rollUpListener)

        rotationAnimator.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Float
            mainImageView.rotation = value
        }

        val scalingSet = AnimatorSet().apply {
            playTogether(scalingAnimator, rotationAnimator)
            interpolator = CustomBounceInterpolator()
        }
        rotationSet.interpolator = BounceInterpolator()


        mRollupSet.playTogether(scalingSet, rotationSet)
    }

    /**
     * Menu inflating, we are getting list of visible items,
     * and use them in method @link initAndAddMenuItem
     * Be careful, don't use non-odd number of menu items
     * FTB works not good for such menus. Anyway you will have an exception
     *
     * @param resId your menu resource id
     */
    private fun inflateMenu(@MenuRes resId: Int) {
        getMenuInflater().inflate(resId, mMenu)
        mData = mMenu.visibleItems.map {
            initAndAddMenuItem(it)
        }
        when {
            isRecording -> recording()
            isPausing -> paused()
        }
        //initMainButton(mMenu.visibleItems.size / 2)
    }


    /**
     * Here we are resolving sizes of your Folding tab bar.
     * Depending on
     * @param measureSpec we can understand what kind of parameters
     * do you using in your layout file
     * In case if you are using wrap_content, we are using @dimen/ftb_size_normal
     * by default
     *
     * In case if you need some custom sizes, please use them)
     */
    private fun resolveAdjustedSize(desiredSize: Int, measureSpec: Int): Int {
        val specMode = View.MeasureSpec.getMode(measureSpec)
        val specSize = View.MeasureSpec.getSize(measureSpec)

        return when (specMode) {
            View.MeasureSpec.UNSPECIFIED ->
                desiredSize

            View.MeasureSpec.AT_MOST ->
                Math.min(desiredSize, specSize)

            View.MeasureSpec.EXACTLY ->
                specSize

            else ->
                desiredSize
        }
    }

    /**
     * Here we are overriding onMeasure and here we are making our control
     * squared
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        /*if (!isAnimating) {
            val preferredSize = getSizeDimension()
            mSize = resolveAdjustedSize(preferredSize, widthMeasureSpec)
            setMeasuredDimension(mSize, mSize)
        }*/
    }

    /**
     * Here we are saving view state
     */
    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(superState).apply {
            selection = selectedIndex
        }
    }

    /**
     * Here we are restoring view state (state, selection)
     */
    override fun onRestoreInstanceState(state: Parcelable?) {
        (state as SavedState).let {
            super.onRestoreInstanceState(it.superState)
            selectedIndex = it.selection
        }
    }

    val scaleAnimator = ValueAnimator.AnimatorUpdateListener { valueAnimator ->
        layoutParams = layoutParams.apply {
            width = valueAnimator.animatedValue as Int
        }
    }

    /**
     * Main button (+/x) initialization
     * Adding listener to the main button click
     */
    private fun initMainButton(mainButtonIndex: Int) {
        mainImageView.setImageResource(drawableResource)
        mainImageView.layoutParams = ViewGroup.LayoutParams(mSize, mSize)
        mainImageView.setOnClickListener {
            onMainButtonClickListener?.onMainButtonClicked()
            animateMenu()
        }
        addView(mainImageView, mainButtonIndex)
        mainImageView.setPadding(itemsPadding, itemsPadding, itemsPadding, itemsPadding)
    }

    /**
     * @param menuItem object from Android Sdk. This is same menu item
     * that you are using e.g in NavigationView or any kind of native menus
     */
    private fun initAndAddMenuItem(menuItem: MenuItemImpl): SelectedMenuItem {
        return SelectedMenuItem(context, selectionColor).apply {
            setImageDrawable(menuItem.icon)
            layoutParams = ViewGroup.LayoutParams(mSize, mSize)
            (layoutParams as? LayoutParams)?.weight = 1f
            setPadding(itemsPadding, itemsPadding, itemsPadding, itemsPadding)
            visibility = View.GONE
            isActivated = menuItem.isChecked
            addView(this, indexCounter)
            menuItemId = menuItem.itemId
            setOnClickListener {
                onFoldingItemClickListener?.onFoldingItemSelected(menuItem)
                menuItem.isChecked = true
                selectedIndex = indexOfChild(this)
                animateMenu()
                when (menuItemId) {
                    R.id.item_record -> onFoldingItemClickListener?.onStartStop()
                    R.id.item_pause_resume -> onFoldingItemClickListener?.onPauseResume()
                    R.id.item_home -> onFoldingItemClickListener?.onOpenHome()
                    R.id.item_setting -> onFoldingItemClickListener?.onOpenSetting()
                    R.id.item_tool -> onFoldingItemClickListener?.onOpenTool()
                }
            }

            indexCounter++
        }
    }

    /**
     * measuredWidth - mSize = 0 we can understand that our menu is closed
     * But on some devices I've found a case when we don't have exactly 0. So
     * now we defined some range to understand what is the state of our menu
     */
    private fun animateMenu() {
        if (!isShowing) {
            expand()
        } else {
            rollUp()
        }
    }


    /**
     * These two public functions can be used to open our menu
     * externally
     */
    fun expand(isRTL: Boolean) {
        mExpandingSet.start()
        isShowing = true
    }

    fun expand() {
        mExpandingSet.start()
        isShowing = true
    }

    fun rollUp() {
        mRollupSet.start()
        isShowing = false
    }

    fun recording() {
        isRecording = true
        isPausing = false
        getStartStopBtn()
            ?.setImageResource(R.drawable.ic_stop_red)
        getPauseResumeBtn()?.apply {
            setImageResource(R.drawable.ic_pause_red)
        }
    }


    fun stopped() {
        isRecording = false
        isPausing = false
        getStartStopBtn()?.apply {
            setImageResource(R.drawable.ic_record_menu)
        }
        getPauseResumeBtn()?.apply {
            isGone = true
        }
    }

    fun paused() {
        isPausing = true
        getPauseResumeBtn()?.setImageResource(R.drawable.ic_play_red)
    }

    private fun getStartStopBtn(): SelectedMenuItem? {
        return mData.firstOrNull { it.menuItemId == R.id.item_record }
    }
    private fun getPauseResumeBtn(): SelectedMenuItem? {
        return mData.firstOrNull { it.menuItemId == R.id.item_pause_resume }
    }

    /**
     * Getting SupportMenuInflater to get all visible items from
     * menu object
     */
    private fun getMenuInflater(): MenuInflater = SupportMenuInflater(context)

    /**
     * Here we should hide all items, and deactivate menu item
     */
    private val rollUpListener = object : Animator.AnimatorListener {
        override fun onAnimationStart(animator: Animator) {
            mData.forEach {
                it.visibility = View.GONE
            }
            onFoldingItemClickListener?.onClosed()
        }

        override fun onAnimationEnd(animator: Animator) {
        }

        override fun onAnimationCancel(animator: Animator) {
        }

        override fun onAnimationRepeat(animator: Animator) {
        }
    }

    /**
     * This listener we need to show our Menu items
     * And also after animation was finished we should activate
     * our SelectableImageView
     */
    private val expandingListener = object : Animator.AnimatorListener {

        override fun onAnimationStart(animator: Animator) {
            mData.forEach {
                if (isRecording) {
                    it.visibility = View.VISIBLE
                } else {
                    if (it.menuItemId != R.id.item_pause_resume) {
                        it.visibility = View.VISIBLE
                    }
                }
            }
            onFoldingItemClickListener?.onOpened()
        }

        override fun onAnimationEnd(animator: Animator) {

        }

        override fun onAnimationCancel(animator: Animator) {
        }

        override fun onAnimationRepeat(animator: Animator) {
        }
    }

    /**
     * Listener for handling events on folding items.
     */
    interface OnFoldingItemSelectedListener {
        /**
         * Called when an item in the folding tab bar menu is selected.

         * @param item The selected item
         * *
         * *
         * @return true to display the item as the selected item
         */
        fun onFoldingItemSelected(item: MenuItem): Boolean
        fun onOpened()
        fun onClosed()
        fun onOpenSetting()
        fun onOpenHome()
        fun onStartStop()
        fun onPauseResume()
        fun onOpenTool()
    }

    /**
     * Listener for handling events on folding main button
     */
    interface OnMainButtonClickedListener {
        /**
         * Called when the main button was pressed
         */
        fun onMainButtonClicked()
    }

    /**
     * We have to save state and selection of our View
     */
    internal class SavedState : View.BaseSavedState {
        var selection: Int = 0

        internal constructor(superState: Parcelable?) : super(superState)

        private constructor(inp: Parcel) : super(inp) {
            selection = inp.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(selection)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

}