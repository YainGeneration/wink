import { useEffect, useRef } from "react";

declare global {
  interface Window {
    kakao: any;
  }
}

const NearBy = () => {
  const mapRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const script = document.createElement("script");
    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${import.meta.env.VITE_KAKAO_API}`;
    script.async = true;

    script.onload = () => {
      console.log("Kakao SDK loaded:", window.kakao);  // 🔥 디버그용
      if (!window.kakao || !window.kakao.maps) {
        console.error("Kakao maps STILL undefined!!");
        return;
      }

      window.kakao.maps.load(() => {
        const container = mapRef.current;
        const options = {
          center: new window.kakao.maps.LatLng(37.5665, 126.978),
          level: 3,
        };
        new window.kakao.maps.Map(container, options);
      });
    };

    document.head.appendChild(script);

    return () => {
      document.head.removeChild(script);
    };
  }, []);

  return (
    <div
      ref={mapRef}
    />
  );
};

export default NearBy;
