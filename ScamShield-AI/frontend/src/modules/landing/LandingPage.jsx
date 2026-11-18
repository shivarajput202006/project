import Navbar from "../../components/layout/Navbar.jsx";
import Footer from "../../components/layout/Footer.jsx";
import HeroSection from "./HeroSection.jsx";
import FeaturesSection from "./FeaturesSection.jsx";
import HowItWorksSection from "./HowItWorksSection.jsx";
import TrustSection from "./TrustSection.jsx";
import CTASection from "./CTASection.jsx";

export default function LandingPage() {
  return (
    <>
      <Navbar />
      <main>
        <HeroSection />
        <FeaturesSection />
        <HowItWorksSection />
        <TrustSection />
        <CTASection />
      </main>
      <Footer />
    </>
  );
}
