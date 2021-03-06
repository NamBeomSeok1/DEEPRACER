if (jQuery) (function ($) {
    //event-tabs
    $.extend($.fn, {
        eventTabsFunc: function () {
            var init = function (obj) {
                var $currentTab = $(obj).find('.event-tabs-nav .is-active a').attr('href');
                $($currentTab).show();

                var $currentTabSm = $($currentTab).find('.event-tabs-nav-sm .is-active a').attr('href');
                $($currentTabSm).show();


                $(obj).find('.event-tabs-nav a').on('click', function () {
                    var $li = $(this).parent('li');
                    if ($li.hasClass("is-active") == false) {
                        $(this).parent('li').siblings('li').removeClass("is-active");
                        $(this).parent('li').addClass("is-active");
                    }

                    $(obj).find('.event-tabs-cont').removeClass('is-active');
                    var $activeTab = $(this).attr('href');
                    $($activeTab).addClass('is-active');

                    if ($($activeTab).children().hasClass('event-tabs-sm')) {
                        $('.event-tabs-sm li').removeClass('is-active');
                        $('.event-tabs-cont-sm').removeClass('is-active');
                        $($activeTab).find('.event-tabs-sm li').first().addClass('is-active');
                        $($activeTab).find('.event-tabs-cont-sm-area').find('.event-tabs-cont-sm').first().addClass('is-active');
                        $($activeTab).find('.event-tabs-nav-sm').scrollLeft(0);
                    }
                    else {
                        
                    }

                    $navOffset = $('.event-tabs-nav').offset().top;
                    $headerH = $('.site-header').outerHeight();

                    $('html').animate({
                        scrollTop: $navOffset - $headerH - 30
                    }, 500);

                    $liIndex = $(this).parent('li').index();
                    $liW = $(this).parent('li').outerWidth();
                    $('.event-tabs-nav').animate({
                        scrollLeft: $liW * $liIndex
                    }, 500);

                    return false;
                });

                $('.event-tabs-nav-sm a').on('click', function () {
                    var $li = $(this).parent('li');
                    if ($li.hasClass("is-active") == false) {
                        $(this).parent('li').siblings('li').removeClass("is-active");
                        $(this).parent('li').addClass("is-active");
                    }

                    $('.event-tabs-cont-sm').removeClass('is-active');
                    var $activeTab = $(this).attr('href');
                    $($activeTab).addClass('is-active');

                    $liIndex = $(this).parent('li').index();
                    $liW = $(this).parent('li').outerWidth();
                    $nav = $(this).parent('li').parent('.event-tabs-nav-sm');

                    $nav.animate({
                        scrollLeft: $liW * $liIndex
                    }, 500);

                    return false;
                });
            };
            init(this);
            return $(this);
        }
    });

    $.extend($.fn, {
        chaFunc: function () {
            var init = function accordion(obj) {
                var controllerSticky = new ScrollMagic.Controller();
                var sceneBtnSticky = new ScrollMagic.Scene({
                    triggerElement: '.site-footer',
                    triggerHook: 1
                }).setClassToggle('.img-cha', 'is-sticky').addTo(controllerSticky);
            };
            init(this);
            return $(this);
        }
    });
})(jQuery);

$(document).ready(function () {
   
    var str = '<li class="finish">                   '  
    	+ '	<div>                              '
    	+ '		<a href="#hd">                 '
    	+ '			<cite>??????????????????</cite>       '
    	+ '			<p>???????????????6??????????????????</p>      '
    	+ '		</a>                           '
    	+ '	</div>                             '
    	+ '</li>                                 '
    	+ '<li class="finish">                   '
    	+ '	<a href="#hmh">                    '
    	+ '		<cite>?????????????????????</cite>          '
    	+ '		<p>????????? ??????????????? 1+1 29,900???</p> '
    	+ '	</a>                               '
    	+ '</li>                                 '
    	+ '<li class="finish">                   '
    	+ '	<a href="#hm">                     '
    	+ '		<cite>?????????</cite>              '
    	+ '		<p>2021??? ????????? ??? ?????? ???????????????</p>  '
    	+ '	</a>                               '
    	+ '</li>                                 '
    	+ '<li class="finish">                   '
    	+ '	<a href="#jg">                     '
    	+ '		<cite>?????????</cite>              '
    	+ '		<p>????????? ?????? ????????? 1+1</p>        '
    	+ '	</a>                               '
    	+ '</li>                                 '
    	+ '<li class="finish">                   '
    	+ '	<a href="#js">                     '
    	+ '		<cite>?????????</cite>              '
    	+ '		<p>??? ???????????? / ?????????6??? ??????</p>     '
    	+ '	</a>                               '
    	+ '</li>                                 '
    	+ '<li class="finish">                   '
    	+ '	<a href="#wg">                     '
    	+ '		<cite>????????????</cite>             '
    	+ '		<p>????????? ???????????? ??? ????????????</p>       '
    	+ '	</a>                               '
    	+ '</li>                                 '
    	+ '<li class="finish">                   '
    	+ '	<a href="#ot">                     '
    	+ '		<cite>?????????</cite>              '
    	+ '		<p>??? ???????????? 9??? / ????????? ?????? ??????</p>'
    	+ '	</a>                               '
    	+ '</li>                                 '
    	+ '<li class="finish">                   '
    	+ '	<a href="#pp">                     '
    	+ '		<cite>???????????????</cite>            '
    	+ '		<p>?????? ??????, 1+1, ??????</p>         '
    	+ '	</a>                               '
    	+ '</li>                                 '
    	+ '<li class="finish">                   '
    	+ '	<a href="#hn">                     '
    	+ '		<cite>????????????</cite>             '
    	+ '		<p>2?????????????????? ??????</p>            '
    	+ '	</a>                               '
    	+ '</li>                                 '
    	+ '<li class="finish">                   '
    	+ '	<a href="#id">                     '
    	+ '		<cite>????????????</cite>             '
    	+ '		<p>?????? ???????????? ????????? ?????? ??????</p>    '
    	+ '	</a>                               '
    	+ '</li>                                 '
    	+ '<li class="finish">                   '
    	+ '	<a href="#se">                     '
    	+ '		<cite>????????????</cite>             '
    	+ '		<p>?????? ?????? ????????? ?????????</p>         '
    	+ '	</a>                               '
    	+ '</li>                                 '
    	+ '<li class="finish">                   '
    	+ '	<a href="#sn">                     '
    	+ '		<cite>??????24</cite>              '
    	+ '		<p>???????????? ?????? ??????</p>            '
    	+ '	</a>                               '
    	+ '</li>                                 '
    	+ '<li class="finish">                   '
    	+ '	<a href="#sw">                     '
    	+ '		<cite>????????????</cite>             '
    	+ '		<p>?????? ????????? ????????? 100???</p>       '
    	+ '	</a>                               '
    	+ '</li>                                 '
    	+ '<li class="finish">                   '
    	+ '	<a href="#sl">                     '
    	+ '		<cite>???????????????</cite>            '
    	+ '		<p>??? ?????? ?????? 20% ??????</p>         '
    	+ '	</a>                               '
    	+ '</li>                                 '
    	+ '<li class="finish">                   '
    	+ '	<a href="#bt">                     '
    	+ '		<cite>????????????</cite>             '
    	+ '		<p>2021??? ???????????? ??? ????????????</p>     '
    	+ '	</a>                               '
    	+ '</li>                                 '
    	+ '<li class="is-active">                '
    	+ '	<a href="#mh">                     '
    	+ '		<cite>???????????????</cite>            '
    	+ '		<p>????????? ?????? + ?????????, 1+1 ??????</p>  '
    	+ '	</a>                               '
    	+ '</li>                                 '
    	+ '<li class="finish">                   '
    	+ '	<a href="#md">                     '
    	+ '		<cite>???????????????</cite>            '
    	+ '		<p>?????????????????? 990???</p>            '
    	+ '	</a>                               '
    	+ '</li>                                 '
    	+ '<li class="finish">                   '
    	+ '	<a href="#ra">                     '
    	+ '		<cite>????????? ???????????????</cite>       '
    	+ '		<p>???????????? ??????</p>                '
    	+ '	</a>                               '
    	+ '</li>                                 '
    	+ '<li class="finish">                   '
    	+ '	<a href="#dd">                     '
    	+ '		<cite>?????????</cite>              '
    	+ '		<p>?????? ????????? ??????????????? 10???</p>      '
    	+ '	</a>                               '
    	+ '</li>                                 '
    	+ '<li>                                  '
    	+ '	<a href="#di">                     '
    	+ '		<cite>??????????????????</cite>           '
    	+ '		<p>?????????, ?????? ?????? ??????</p>         '
    	+ '	</a>                               '
    	+ '</li>                                 '
    	+ '<li class="finish">                   ';
    
    var today = moment(new Date()).format('YYYY-MM-DD');
	if (today >= '2021-02-15') {
		$('ul.event-tabs-nav').html(str);
	}
    
    //event-tabs
    $('.event-tabs').eventTabsFunc();
    $('.img-cha').chaFunc();
});


//snow
(function () {
    function ready(fn) {
        if (document.readyState != 'loading') {
            fn();
        } else {
            document.addEventListener('DOMContentLoaded', fn);
        }
    }

    function makeSnow(el) {
        var ctx = el.getContext('2d');
        var width = 0;
        var height = 0;
        var particles = [];

        var Particle = function () {
            this.x = this.y = this.dx = this.dy = 0;
            this.reset();
        }

        Particle.prototype.reset = function () {
            this.y = Math.random() * height;
            this.x = Math.random() * width;
            this.dx = (Math.random() * 1) - 0.5;
            this.dy = (Math.random() * 0.5) + 0.5;
        }

        function createParticles(count) {
            if (count != particles.length) {
                particles = [];
                for (var i = 0; i < count; i++) {
                    particles.push(new Particle());
                }
            }
        }

        function onResize() {
            width = window.innerWidth;
            height = window.innerHeight;
            el.width = width;
            el.height = height;

            createParticles((width * height) / 10000);
        }

        function updateParticles() {
            ctx.clearRect(0, 0, width, height);
            ctx.fillStyle = '#fff';

            particles.forEach(function (particle) {
                particle.y += particle.dy;
                particle.x += particle.dx;

                if (particle.y > height) {
                    particle.y = 0;
                }

                if (particle.x > width) {
                    particle.reset();
                    particle.y = 0;
                }

                ctx.beginPath();
                ctx.arc(particle.x, particle.y, 5, 0, Math.PI * 2, false);
                ctx.fill();
            });

            window.requestAnimationFrame(updateParticles);
        }

        onResize();
        updateParticles();

        window.addEventListener('resize', onResize);
    }

    ready(function () {
        var canvas = document.getElementById('snow');
        makeSnow(canvas);
    });
})();